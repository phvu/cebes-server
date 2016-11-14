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
 * Created by phvu on 06/10/16.
 */

package io.cebes.df.types

import io.cebes.df.types.storage.StorageType

object VariableTypes {

  sealed abstract class VariableType(val name: String, val isNumeric: Boolean,
                                     val isCategorical: Boolean,
                                     val validStorageTypes: Seq[StorageType]) {
    override def toString: String = name
  }

  case object DISCRETE extends VariableType("Discrete", true, false,
    Seq(StorageTypes.ByteType, StorageTypes.ShortType, StorageTypes.IntegerType, StorageTypes.LongType))

  case object CONTINUOUS extends VariableType("Continuous", true, false,
    Seq(StorageTypes.FloatType, StorageTypes.DoubleType))

  /**
    * Categorical variable without rank
    */
  case object NOMINAL extends VariableType("Nominal", false, true,
    Seq(StorageTypes.StringType, StorageTypes.BooleanType,
      StorageTypes.ByteType, StorageTypes.ShortType, StorageTypes.IntegerType, StorageTypes.LongType,
      StorageTypes.FloatType, StorageTypes.DoubleType,
      StorageTypes.DateType, StorageTypes.TimestampType))

  /**
    * Categorical variable with a rank, an order
    */
  case object ORDINAL extends VariableType("Ordinal", false, true,
    Seq(StorageTypes.StringType, StorageTypes.BooleanType,
      StorageTypes.ByteType, StorageTypes.ShortType, StorageTypes.IntegerType, StorageTypes.LongType,
      StorageTypes.FloatType, StorageTypes.DoubleType,
      StorageTypes.DateType, StorageTypes.TimestampType))

  case object TEXT extends VariableType("Text", false, false,
    Seq(StorageTypes.StringType))

  case object DATETIME extends VariableType("DateTime", false, false,
    Seq(StorageTypes.DateType, StorageTypes.TimestampType))

  case object ARRAY extends VariableType("Array", false, false,
    Seq(StorageTypes.VectorType, StorageTypes.BinaryType))

  val values = Seq(DISCRETE, CONTINUOUS, NOMINAL, ORDINAL, TEXT, ARRAY)

  def fromString(name: String): VariableType = values.find(_.name == name) match {
    case Some(t) => t
    case None => throw new IllegalArgumentException(s"Unrecognized variable type: $name")
  }

  /**
    * Rude guess to infer variable type from storage type
    *
    * @param storageType storage type
    * @return variable types
    */
  def fromStorageType(storageType: StorageType): VariableType = {
    storageType match {
      case StorageTypes.BinaryType | StorageTypes.VectorType =>
        VariableTypes.ARRAY
      case StorageTypes.TimestampType | StorageTypes.DateType  =>
        VariableTypes.DATETIME
      case StorageTypes.BooleanType => VariableTypes.NOMINAL
      case StorageTypes.ByteType | StorageTypes.ShortType |
           StorageTypes.IntegerType | StorageTypes.LongType =>
        VariableTypes.DISCRETE
      case StorageTypes.FloatType | StorageTypes.DoubleType =>
        VariableTypes.CONTINUOUS
      case StorageTypes.StringType => VariableTypes.TEXT
    }
  }
}
