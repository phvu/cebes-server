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
package io.cebes.pipeline.factory

import com.google.inject.Inject
import io.cebes.common.HasId
import io.cebes.pipeline.json.PipelineDef
import io.cebes.pipeline.models.{Pipeline, Stage}

import scala.collection.mutable
import scala.concurrent.ExecutionContext

class PipelineFactory @Inject()(private val stageFactory: StageFactory) {

  /**
    * Create the pipeline object from the given definition.
    * This is normally used to create a new Pipeline from user's definition.
    *
    * Note that this will not wire the inputs of stages. That will only be done when
    * [[Pipeline.run()]] is called.
    */
  def create(pipelineDef: PipelineDef)(implicit ec: ExecutionContext): Pipeline = {
    val id = pipelineDef.id.getOrElse(HasId.randomId)

    val stageMap = mutable.Map.empty[String, Stage]
    pipelineDef.stages.map { s =>
      val stage = stageFactory.create(s)
      require(!stageMap.contains(stage.getName), s"Duplicated stage name: ${stage.getName}")
      stageMap.put(stage.getName, stage)
    }

    Pipeline(id, stageMap.toMap, pipelineDef.copy(id = Some(id)))
  }
}
