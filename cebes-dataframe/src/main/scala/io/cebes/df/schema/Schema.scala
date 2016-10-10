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
 * Created by phvu on 26/09/16.
 */

package io.cebes.df.schema


class Schema(val columns: Seq[Column]) {

  /**
    * Number of columns
    *
    * @return a Long
    */
  def numCols: Long = columns.length

  /**
    * Get the column as an Option[Column]
    *
    * @param name name of the column to get, case-insensitive
    * @return Option[Column]
    */
  def getColumnOptional(name: String): Option[Column] = {
    columns.find(_.name.equalsIgnoreCase(name))
  }

  /**
    * Get the column object. To get the column as an Option[Column], use [[getColumnOptional()]].
    *
    * @param name name of the column to get, case-insensitive
    * @return [[Column]] object if found, or [[IllegalArgumentException]] otherwise
    */
  def getColumn(name: String): Column = {
    val col = getColumnOptional(name)
    if (col.isEmpty) {
      throw new IllegalArgumentException(s"Column name not found: $name")
    }
    col.get
  }

  override def toString: String = columns.map(c => s"${c.name} ${c.storageType.toString}").mkString(", ")

  def copy(): Schema = new Schema(columns.map(_.copy()))

}

object Schema {

  def fromString(schemaStr: String): Schema = {
    val cols = schemaStr.split(",").map { col =>
      col.stripPrefix(" ").stripSuffix(" ").split(" ").filter(_.length > 0) match {
        case Array(colName, colType) => new Column(colName, StorageTypes.fromString(colType))
        case t => throw new IllegalArgumentException(s"Unrecognized column specification: ${t.toString}")
      }
    }
    new Schema(cols)
  }
}
