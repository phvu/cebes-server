/* Copyright 2016 The Cebes Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, version 2.0 (the "License").
 * You may not use this work except in compliance with the License,
 * which is available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package io.cebes.pipeline.models

import java.util.concurrent.locks.{ReadWriteLock, ReentrantReadWriteLock}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/**
  * A Pipeline stage, with a name, inputs, outputs
  * The Stage is designed around the concept of [[Future]].
  * Stage.output(s) is the output at slot with name `s`, of type [[Future[Any]] which can be waited on,
  * and cached if the input doesn't change.
  *
  * Note that this makes the stages to be stateful, which is somehow against the philosophy of Scala,
  * but we do it for the sake of runtime "efficiency",
  * although at the price of more code with all kinds of locks.
  */
trait Stage extends Inputs {

  /**
    * Implement this to do the real job of transforming inputs into outputs
    */
  protected def run(inputs: SlotValueMap): SlotValueMap

  /////////////////////////////////////////////////////////////////////////////
  // name of a stage is nothing more than an input slot
  /////////////////////////////////////////////////////////////////////////////

  val name: InputSlot[String] = inputSlot[String]("name", "Name of this stage",
    Some(getClass.getSimpleName.toLowerCase), SlotValidators.isValidStageName)

  def getName: String = input(name).get

  def setName(newName: String): this.type = {
    input(name, newName)
  }

  /////////////////////////////////////////////////////////////////////////////
  // outputs
  /////////////////////////////////////////////////////////////////////////////

  /** list of all output slots produced by this component */
  private lazy val _outputs: Array[OutputSlot[_]] = getSlotMembers[OutputSlot[_]]

  /** Lock object for `outputMap` */
  private val outputLock: ReadWriteLock = new ReentrantReadWriteLock()

  /** List that holds the outputs */
  private var outputMap: Option[Map[OutputSlot[Any], Future[Any]]] = None

  /** Gets output slot by its name. */
  final def getOutput(slotName: String): OutputSlot[Any] = {
    _outputs.find(_.name == slotName).getOrElse {
      throw new NoSuchElementException(s"Slot $slotName does not exist.")
    }
  }

  /**
    * Return the actual output at the given index.
    */
  def output[T](outputSlot: OutputSlot[T])(implicit ec: ExecutionContext): Future[T] = {
    require(_outputs.contains(outputSlot), s"Output name ${outputSlot.name} not found in stage $toString")

    inputLock.readLock().lock()
    outputLock.readLock().lock()
    if (outputMap.isEmpty || hasNewInput) {
      outputLock.readLock().unlock()
      outputLock.writeLock().lock()
      try {
        outputMap = Some(computeOutput)
        hasNewInput = false

        // Downgrade by acquiring read lock before releasing write lock
        outputLock.readLock().lock()
      } finally {
        inputLock.readLock().unlock()
        outputLock.writeLock().unlock()
      }
    } else {
      inputLock.readLock().unlock()
    }

    try {
      outputMap.get.get(outputSlot.asInstanceOf[OutputSlot[Any]]) match {
        case Some(f) => f.asInstanceOf[Future[T]]
        case None =>
          throw new NoSuchElementException(s"Stage $toString: slot named ${outputSlot.name} is not computed.")
      }
    } finally {
      outputLock.readLock().unlock()
    }
  }

  /**
    * Compute the output, check the types and number of output slots
    */
  private def computeOutput(implicit ec: ExecutionContext): Map[OutputSlot[Any], Future[Any]] = {
    val futureOutput = withAllInputs { inps =>
      // remove the default slot containing the name of this stage
      // subclasses don't need the name in their "run()" function
      inps.remove(name)

      val out = run(inps)
      _outputs.foreach { s =>
        if (!out.contains(s)) {
          throw new IllegalArgumentException(s"Stage $toString: output doesn't contain result for slot ${s.name}")
        }
        Option(out(s)) match {
          case Some(v) =>
            if (!s.messageClass.isAssignableFrom(v.getClass)) {
              throw new IllegalArgumentException(s"Stage $toString: output slot ${s.name} expects type " +
                s"${s.messageClass.getSimpleName}, but got value ${v.toString} of type ${v.getClass.getSimpleName}")
            }
          case None =>
        }
      }
      out
    }
    _outputs.map { s =>
      s -> futureOutput.map(seq => seq(s))
    }.toMap
  }

  /** Create an **output** slot of the given type */
  final protected def outputSlot[T](name: String, doc: String, defaultValue: Option[T],
                                    validator: SlotValidator[T] = SlotValidators.default[T])
                                   (implicit tag: ClassTag[T]): OutputSlot[T] = {
    OutputSlot[T](name, doc, defaultValue, validator)
  }

  override def toString: String = s"${getClass.getSimpleName}(name=$getName)"
}
