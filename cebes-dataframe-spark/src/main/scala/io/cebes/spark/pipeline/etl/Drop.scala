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
package io.cebes.spark.pipeline.etl

import io.cebes.df.Dataframe
import io.cebes.pipeline.models.StringArrayParam
import io.cebes.pipeline.stages.UnaryTransformer

/**
  * Returns a new Dataframe with columns dropped.
  * This is a no-op if schema doesn't contain column name(s).
  */
class Drop extends UnaryTransformer {

  val colNames = StringArrayParam("colNames", Some(Array()),
    "List of column names to be dropped")

  override protected def transform(df: Dataframe): Dataframe = {
    df.drop(param(colNames))
  }
}
