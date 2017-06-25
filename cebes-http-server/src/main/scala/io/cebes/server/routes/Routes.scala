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
 *
 * Created by phvu on 24/08/16.
 */

package io.cebes.server.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.cebes.server.routes.HttpJsonProtocol._
import io.cebes.server.routes.auth.AuthHandler
import io.cebes.server.routes.df.DataframeHandler
import io.cebes.server.routes.model.ModelHandler
import io.cebes.server.routes.pipeline.PipelineHandler
import io.cebes.server.routes.result.ResultHandler
import io.cebes.server.routes.storage.StorageHandler
import io.cebes.server.routes.test.TestHandler

trait Routes extends ApiErrorHandler with AuthHandler with DataframeHandler with PipelineHandler with ModelHandler
  with StorageHandler with ResultHandler with TestHandler {

  val routes: Route =
    pathPrefix(Routes.API_VERSION) {
      authApi ~
        dataframeApi ~
        pipelineApi ~
        modelApi ~
        storageApi ~
        resultApi ~
        testApi
    } ~
      (path("") & get) {
        getFromResource("public/index.html")
      } ~
      (path("version") & get) {
        complete(VersionResponse(Routes.API_VERSION))
      }
}

object Routes {

  val API_VERSION = "v1"
}
