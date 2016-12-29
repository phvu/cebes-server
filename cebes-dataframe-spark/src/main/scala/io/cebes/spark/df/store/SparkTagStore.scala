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
 * Created by phvu on 29/12/2016.
 */

package io.cebes.spark.df.store

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import io.cebes.common.Tag
import io.cebes.df.store.TagStore
import io.cebes.persistence.jdbc.{JdbcPersistenceBuilder, JdbcPersistenceColumn, TableNames}
import io.cebes.prop.types.MySqlBackendCredentials

@Singleton class SparkTagStore @Inject()
(mySqlCreds: MySqlBackendCredentials) extends TagStore {

  private val jdbcStore = JdbcPersistenceBuilder.newBuilder[Tag, UUID]()
    .withCredentials(mySqlCreds.url, mySqlCreds.userName, mySqlCreds.password,
      TableNames.TAG_STORE, mySqlCreds.driver)
    .withValueSchema(Seq(
      JdbcPersistenceColumn("uuid", "VARCHAR(200)")
    ))
    .withValueToSeq(v => Seq(v.toString))
    .withSqlToValue { case (_, v) => UUID.fromString(v.getString(1)) }
    .build()

  // TODO: this is shitty. Should find another way to inject the Persistence
  // https://github.com/google/guice/wiki/FrequentlyAskedQuestions#how-to-inject-class-with-generic-type
  override def add(tag: Tag, id: UUID): Unit = jdbcStore.insert(tag, id)

  override def remove(tag: Tag): Unit = jdbcStore.remove(tag)

  override def elements: Iterator[(Tag, UUID)] = jdbcStore.elements
}
