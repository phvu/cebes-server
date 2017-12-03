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
package io.cebes.serving.common

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Singleton

import scala.concurrent.ExecutionContextExecutor

/**
  * Contains the instantiated objects for the actor system used in akka-http.
  */
@Singleton class ServingActor {

  val actorSystem: ActorSystem = ActorSystem("CebesPipelineServing")
  val actorExecutor: ExecutionContextExecutor = actorSystem.dispatcher
  val actorMaterializer: ActorMaterializer = ActorMaterializer()(actorSystem)
}
