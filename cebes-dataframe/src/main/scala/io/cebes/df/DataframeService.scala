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
 * Created by phvu on 23/08/16.
 */

package io.cebes.df

import java.util.UUID

trait DataframeService {

  /**
    * Executes a SQL query, returning the result as a [[Dataframe]].
    *
    * @param sqlText the SQL command to run
    * @return a [[Dataframe]] object
    */
  def sql(sqlText: String): Dataframe

  /**
    * Sample the given [[Dataframe]]
    */
  def sample(df: UUID, withReplacement: Boolean, fraction: Double, seed: Long): Dataframe
}
