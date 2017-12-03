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
package io.cebes.serving.spark

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging
import io.cebes.repository.client.RepositoryClient
import io.cebes.serving.ServingConfiguration
import io.cebes.serving.common.{ServingActor, ServingManager}
import io.cebes.spark.json.CebesSparkJsonProtocol._
import io.cebes.tag.Tag

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Implementation of [[ServingManager]] on Spark
  * Serve as a store of pipelines being served, that can be looked-up using their servingNames.
  */
class SparkServingManager @Inject()(servingConfiguration: ServingConfiguration,
                                    repoClient: RepositoryClient,
                                    servingActor: ServingActor)
  extends ServingManager with LazyLogging {

  private implicit val actorSystem: ActorSystem = servingActor.actorSystem
  private implicit val actorExecutor: ExecutionContextExecutor = servingActor.actorExecutor
  private implicit val actorMaterializer: ActorMaterializer = servingActor.actorMaterializer

  private lazy val servings: Map[String, Future[PipelineInformation]] = loadPipelines()

  override def getPipeline(servingName: String): Future[PipelineInformation] = {
    servings.get(servingName) match {
      case None => throw new IllegalArgumentException(s"Serving name not found: $servingName")
      case Some(futurePplInfo) => futurePplInfo
    }
  }

  private def loadPipelines(): Map[String, Future[PipelineInformation]] = {
    servingConfiguration.pipelines.map { servingPl =>
      val repoTag = Tag.fromString(servingPl.pipelineTag)

      val futurePpl = repoClient.download(repoTag, servingPl.userName, servingPl.password)
      futurePpl.onFailure {
        case ex => logger.error(s"Failed to download ${servingPl.pipelineTag}", ex)
      }

      servingPl.servingName -> futurePpl.map { ppl =>
        PipelineInformation(ppl, servingPl.slotNamings)
      }
    }.toMap
  }
}
