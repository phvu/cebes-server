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

package io.cebes.pipeline.json

import io.cebes.json.GenericJsonProtocol
import spray.json._


trait PipelineJsonProtocol extends DefaultJsonProtocol with GenericJsonProtocol {

  implicit object ValueDefFormat extends JsonFormat[ValueDef] {

    override def write(obj: ValueDef): JsValue = writeJson(obj.value)

    override def read(json: JsValue): ValueDef = ValueDef(readJson(json))
  }

  implicit val stageOutputDefFormat: RootJsonFormat[StageOutputDef] = jsonFormat2(StageOutputDef)
  implicit val dataframeMessageDefFormat: RootJsonFormat[DataframeMessageDef] = jsonFormat0(DataframeMessageDef)
  implicit val sampleMessageDefFormat: RootJsonFormat[SampleMessageDef] = jsonFormat0(SampleMessageDef)
  implicit val modelMessageDefFormat: RootJsonFormat[ModelMessageDef] = jsonFormat0(ModelMessageDef)
  implicit val columnDefFormat: RootJsonFormat[ColumnDef] = jsonFormat0(ColumnDef)

  implicit object PipelineMessageDefFormat extends JsonFormat[PipelineMessageDef] {

    override def write(obj: PipelineMessageDef): JsValue = obj match {
      case value: ValueDef =>
        JsArray(JsString(value.getClass.getSimpleName), value.toJson)
      case stageOutput: StageOutputDef =>
        JsArray(JsString(stageOutput.getClass.getSimpleName), stageOutput.toJson)
      case dfMsg: DataframeMessageDef =>
        JsArray(JsString(dfMsg.getClass.getSimpleName), dfMsg.toJson)
      case sampleMsg: SampleMessageDef =>
        JsArray(JsString(sampleMsg.getClass.getSimpleName), sampleMsg.toJson)
      case modelMsg: ModelMessageDef =>
        JsArray(JsString(modelMsg.getClass.getSimpleName), modelMsg.toJson)
      case colDef: ColumnDef =>
        JsArray(JsString(colDef.getClass.getSimpleName), colDef.toJson)
      case other => serializationError(s"Couldn't serialize type ${other.getClass.getCanonicalName}")
    }

    override def read(json: JsValue): PipelineMessageDef = json match {
      case jsArr: JsArray =>
        require(jsArr.elements.size == 2, "Expected a JsArray of 2 elements")
        jsArr.elements.head match {
          case JsString(ValueDef.getClass.getSimpleName) => jsArr.elements.last.convertTo[ValueDef]
          case JsString(StageOutputDef.getClass.getSimpleName) => jsArr.elements.last.convertTo[StageOutputDef]
          case JsString(DataframeMessageDef.getClass.getSimpleName) =>
            jsArr.elements.last.convertTo[DataframeMessageDef]
          case JsString(SampleMessageDef.getClass.getSimpleName) =>
            jsArr.elements.last.convertTo[SampleMessageDef]
          case JsString(ModelMessageDef.getClass.getSimpleName) =>
            jsArr.elements.last.convertTo[ModelMessageDef]
          case JsString(ColumnDef.getClass.getSimpleName) =>
            jsArr.elements.last.convertTo[ColumnDef]
          case _ =>
            deserializationError(s"Unable to deserialize ${jsArr.compactPrint}")
        }
      case other =>
        deserializationError(s"Expected a JsObject, got ${other.getClass.getCanonicalName}")
    }
  }

  implicit val stageDefFormat: RootJsonFormat[StageDef] = jsonFormat4(StageDef)
  implicit val pipelineDefFormat: RootJsonFormat[PipelineDef] = jsonFormat2(PipelineDef)
  implicit val pipelineRunDefFormat: RootJsonFormat[PipelineRunDef] = jsonFormat3(PipelineRunDef)
}

object PipelineJsonProtocol extends PipelineJsonProtocol
