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

import io.cebes.df.sample.DataSample

trait DataframeService {

  /**
    * Executes a SQL query, returning the result as a [[Dataframe]].
    *
    * @param sqlText the SQL command to run
    * @return a [[Dataframe]] object
    */
  def sql(sqlText: String): Dataframe

  /**
    * Returns the number of rows in the given [[Dataframe]]
    */
  def count(dfId: UUID): Long

  /**
    * Take some rows from the given [[Dataframe]], returns a [[DataSample]]
    */
  def take(dfId: UUID, n: Int): DataSample

  /**
    * Sample the given [[Dataframe]]
    */
  def sample(dfId: UUID, withReplacement: Boolean, fraction: Double, seed: Long): Dataframe

  ////////////////////////////////////////////////////////////////////////////////////
  // Data exploration
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * Sort the given [[Dataframe]]
    */
  def sort(dfId: UUID, sortExprs: Column*): Dataframe

  /**
    * Returns a new Dataframe with columns dropped.
    * This is a no-op if schema doesn't contain column name(s).
    */
  def drop(dfId: UUID, colNames: Seq[String]): Dataframe

  /**
    * Returns a new Dataframe that contains only the unique rows from this Dataframe.
    */
  def dropDuplicates(dfId: UUID, colNames: Seq[String]): Dataframe

  /**
    * Drop rows with NA
    */
  def dropNA(dfId: UUID, minNonNulls: Int, cols: Seq[String]): Dataframe

  /**
    * fill NA cells with given value, either double or string
    */
  def fillNA(dfId: UUID, value: Either[String, Double], cols: Seq[String]): Dataframe

  /**
    * Fill NA cells using the given map.
    * The key of the map is the column name, and the value of the map is the replacement value.
    * The value must be of the following type: `Int`, `Long`, `Float`, `Double`, `String`, `Boolean`.
    * Replacement values are cast to the column data type.
    */
  def fillNA(dfId: UUID, valueMap: Map[String, Any]): Dataframe

  /**
    * Replaces values matching keys in `replacement` map.
    * Key and value of `replacement` map must have the same type, and
    * can only be doubles , strings or booleans.
    */
  def replace[T](dfId: UUID, cols: Seq[String], replacement: Map[T, T]): Dataframe

  ////////////////////////////////////////////////////////////////////////////////////
  // SQL-related functions
  ////////////////////////////////////////////////////////////////////////////////////

  /**
    * Returns a new [[Dataframe]] by adding a column or replacing
    * the existing column that has the same name (case-insensitive).
    */
  def withColumn(dfId: UUID, colName: String, col: Column): Dataframe

  /**
    * Returns a new [[Dataframe]] with a column renamed.
    */
  def withColumnRenamed(dfId: UUID, existingName: String, newName: String): Dataframe

  /**
    * Selects a set of columns based on expressions.
    */
  def select(dfId: UUID, columns: Column*): Dataframe

  /**
    * Filters rows using the given condition.
    */
  def where(dfId: UUID, column: Column): Dataframe

  /**
    * Returns a new Dataframe with an alias set.
    */
  def alias(dfId: UUID, alias: String): Dataframe

  /**
    * Join with another [[Dataframe]], using the given join expression.
    */
  def join(leftDfId: UUID, rightDfId: UUID, joinExprs: Column, joinType: String): Dataframe

  /**
    * Returns a new [[Dataframe]] by taking the first `n` rows.
    */
  def limit(dfId: UUID, n: Int): Dataframe

  /**
    * Returns a new Dataframe containing union of rows in this Dataframe and another Dataframe
    * (without deduplication)
    *
    * @group sql-api
    */
  def union(dfId: UUID, otherDfId: UUID): Dataframe

  /**
    * Returns a new Dataframe containing rows only in both this Dataframe and another Dataframe.
    *
    * Note that, equality checking is performed directly on the encoded representation of the data
    * and thus is not affected by a custom `equals` function.
    *
    * @group sql-api
    */
  def intersect(dfId: UUID, otherDfId: UUID): Dataframe

  /**
    * Returns a new Dataframe containing rows in this Dataframe but not in another Dataframe.
    * This is equivalent to `EXCEPT` in SQL.
    *
    * Note that, equality checking is performed directly on the encoded representation of the data
    * and thus is not affected by a custom `equals` function.
    *
    * @group sql-api
    */
  def except(dfId: UUID, otherDfId: UUID): Dataframe

  /**
    * Marks a DataFrame as small enough for use in broadcast joins.
    * @group sql-api
    */
  def broadcast(dfId: UUID): Dataframe
}
