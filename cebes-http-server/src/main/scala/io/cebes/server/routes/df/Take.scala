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
 * Created by phvu on 13/12/2016.
 */

package io.cebes.server.routes.df

import com.google.inject.Inject
import io.cebes.df.DataframeService
import io.cebes.df.sample.DataSample
import io.cebes.server.result.ResultStorage
import io.cebes.server.routes.common.AsyncExecutor

import scala.concurrent.{ExecutionContext, Future}

/**
  * Sample a Dataframe, returns a sample of the data
  */
class Take @Inject()(dfService: DataframeService, override val resultStorage: ResultStorage)
  extends AsyncExecutor[LimitRequest, DataSample, DataSample] {

  /**
    * Implement this to do the real work
    */
  override protected def runImpl(requestEntity: LimitRequest)
                                (implicit ec: ExecutionContext): Future[DataSample] = Future {
    dfService.take(requestEntity.df, requestEntity.n)
  }

  override protected def transformResult(requestEntity: LimitRequest, result: DataSample): Option[DataSample] = {
    Some(result)
  }
}
