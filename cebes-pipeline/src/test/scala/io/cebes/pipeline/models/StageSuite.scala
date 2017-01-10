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

import org.scalatest.FunSuite

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class StageSuite extends FunSuite {

  implicit val ec: ExecutionContext = ExecutionContext.global

  test("bad input") {

    val s = new StageFoo("foo")
    val ex = intercept[NoSuchElementException] {
      Await.result(s.output(0), Duration.Inf)
    }
    assert(ex.getMessage === "Slot named dfIn is not specified.")

    s.input(0, Future(new ValueMessage()))
    val ex2 = intercept[IllegalArgumentException] {
      Await.result(s.output(0), Duration.Inf)
    }
    assert(ex2.getMessage.contains("invalid input type at slot #0 (dfIn), " +
      "expected a DataframeMessage, got ValueMessage"))

    s.input(0, Future(new DataframeMessage()))
    assert(Await.result(s.output(0), Duration.Inf).isInstanceOf[DataframeMessage])

    val ex3 = intercept[IllegalArgumentException] {
      s.input(2, Future(new DataframeMessage()))
    }
    assert(ex3.getMessage.contains("Invalid input index: 2. Has to be in [0, 1)"))
  }

  test("bad output size") {
    val stage1 = new StageBadOutputSize("stage1")

    val ex1 = intercept[IllegalArgumentException] {
      stage1.input(0, Future(new DataframeMessage()))
    }
    assert(ex1.getMessage.contains("Invalid input index: 0. Has to be in [0, 0)"))

    val ex2 = intercept[IllegalArgumentException] {
      Await.result(stage1.output(0), Duration.Inf)
    }
    assert(ex2.getMessage.contains("Stage StageBadOutputSize(name=stage1) has 1 output, " +
      "but its run() function returns 2 output"))

    val ex3 = intercept[IllegalArgumentException] {
      Await.result(stage1.output(1), Duration.Inf)
    }
    assert(ex3.getMessage.contains("Stage StageBadOutputSize(name=stage1): " +
      "invalid output index 1, has to be in [0, 1)"))
  }

  test("bad output type") {
    val stage1 = new StageBadOutputType("stage1")

    val ex1 = intercept[IllegalArgumentException] {
      stage1.input(0, Future(new DataframeMessage()))
    }
    assert(ex1.getMessage.contains("Invalid input index: 0. Has to be in [0, 0)"))

    val ex2 = intercept[IllegalArgumentException] {
      Await.result(stage1.output(0), Duration.Inf)
    }
    assert(ex2.getMessage.contains("Stage StageBadOutputType(name=stage1): invalid output type at slot #0 (m), " +
      "expected a ModelMessage, got DataframeMessage"))
  }

  test("chaining") {
    val s1 = new StageFoo("s1")
    val s2 = new StageTwoInputs("s2")

    s1.input(0, Future(new DataframeMessage()))
    s2.input(0, s1.output(0))
    val ex1 = intercept[NoSuchElementException] {
      Await.result(s2.output(0), Duration.Inf)
    }
    assert(ex1.getMessage.contains("Slot named m is not specified."))

    s2.input(1, Future(new ModelMessage()))
    assert(Await.result(s2.output(0), Duration.Inf).isInstanceOf[DataframeMessage])
  }
}

class StageFoo(override val name: String)(implicit val ec: ExecutionContext) extends Stage {

  override protected val _inputs: Seq[Slot[PipelineMessage]] = Seq(DataframeSlot("dfIn"))

  override protected val _outputs: Seq[Slot[PipelineMessage]] = Seq(DataframeSlot("dfOut"))

  override protected def run(inputs: Seq[PipelineMessage]): Seq[PipelineMessage] = {
    assert(inputs.size == 1)
    assert(inputs.head.isInstanceOf[DataframeMessage])
    Seq(new DataframeMessage())
  }
}

class StageTwoInputs(override val name: String)(implicit val ec: ExecutionContext) extends Stage {

  override protected val _inputs: Seq[Slot[PipelineMessage]] = Seq(DataframeSlot("dfIn"), ModelSlot("m"))

  override protected val _outputs: Seq[Slot[PipelineMessage]] = Seq(DataframeSlot("dfOut"))

  override protected def run(inputs: Seq[PipelineMessage]): Seq[PipelineMessage] = {
    assert(inputs.size == 2)
    assert(inputs.head.isInstanceOf[DataframeMessage])
    assert(inputs.last.isInstanceOf[ModelMessage])
    Seq(new DataframeMessage())
  }
}

class StageBar(override val name: String)(implicit val ec: ExecutionContext) extends Stage {

  override protected val _inputs: Seq[Slot[PipelineMessage]] = Seq(DataframeSlot("df1"))

  override protected val _outputs: Seq[Slot[PipelineMessage]] = Seq(ModelSlot("m"))

  override protected def run(inputs: Seq[PipelineMessage]): Seq[PipelineMessage] = {
    assert(inputs.size == 1)
    assert(inputs.head.isInstanceOf[DataframeMessage])
    Seq(new ModelMessage())
  }
}

class StageBadOutputType(override val name: String)(implicit val ec: ExecutionContext) extends Stage {

  override protected val _inputs: Seq[Slot[PipelineMessage]] = Nil

  override protected val _outputs: Seq[Slot[PipelineMessage]] = Seq(ModelSlot("m"))

  override protected def run(inputs: Seq[PipelineMessage]): Seq[PipelineMessage] = {
    assert(inputs.isEmpty)
    Seq(new DataframeMessage())
  }
}

class StageBadOutputSize(override val name: String)(implicit val ec: ExecutionContext) extends Stage {

  override protected val _inputs: Seq[Slot[PipelineMessage]] = Nil

  override protected val _outputs: Seq[Slot[PipelineMessage]] = Seq(ModelSlot("m"))

  override protected def run(inputs: Seq[PipelineMessage]): Seq[PipelineMessage] = {
    assert(inputs.isEmpty)
    Seq(new ModelMessage(), new DataframeMessage())
  }
}