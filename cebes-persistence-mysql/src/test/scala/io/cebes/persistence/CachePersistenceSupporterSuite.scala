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
 * Created by phvu on 29/11/2016.
 */

package io.cebes.persistence

import java.util.UUID

import com.google.common.cache.{CacheBuilder, LoadingCache}
import com.google.common.util.concurrent.UncheckedExecutionException
import io.cebes.persistence.cache.CachePersistenceSupporter
import io.cebes.persistence.helpers.TestPropertyHelper
import io.cebes.persistence.jdbc.{JdbcPersistenceBuilder, JdbcPersistenceColumn}
import org.scalatest.FunSuite

class CachePersistenceSupporterSuite extends FunSuite with TestPropertyHelper {

  test("in-memory persistence") {
    val persistence = new InMemoryPersistence[Integer, String](Map(int2Integer(100) -> "Hundred"))
    val supporter = new CachePersistenceSupporter[Integer, String](persistence)

    val cache: LoadingCache[Integer, String] = CacheBuilder.newBuilder()
      .maximumSize(1)
      .removalListener(supporter)
      .build[Integer, String](supporter)

    // load element that is already in the supporter, but not in cache
    assert(cache.size() === 0)
    assert(cache.get(100) === "Hundred")
    assert(cache.size() === 1)

    val ex = intercept[UncheckedExecutionException](cache.get(200))
    assert(ex.getCause.isInstanceOf[NoSuchElementException])

    cache.put(int2Integer(200), "Two Hundred")
    assert(cache.size() === 1)
    assert(cache.get(200) === "Two Hundred")
    assert(cache.get(100) === "Hundred")

    // replace
    cache.put(100, "One Hundred")
    assert(cache.get(100) === "One Hundred")
  }

  test("MySql persistence", JdbcTestsEnabled) {
    val persistence = JdbcPersistenceBuilder.newBuilder[UUID, String]()
      .withCredentials(properties.jdbcUrl, properties.jdbcUsername, properties.jdbcPassword,
        "test_persistence_string", properties.jdbcDriver)
      .withValueSchema(Seq(JdbcPersistenceColumn("field1", "VARCHAR(200)")))
      .withValueToSeq(v => Seq(v))
      .withSqlToValue(s => s.getString("field1"))
      .build()
    val supporter = new CachePersistenceSupporter(persistence)

    val cache: LoadingCache[UUID, String] = CacheBuilder.newBuilder()
      .maximumSize(1)
      .removalListener(supporter)
      .build[UUID, String](supporter)

    // load element that is already in the supporter, but not in cache
    val uuid1 = UUID.randomUUID()
    val ex = intercept[UncheckedExecutionException](cache.get(uuid1))
    assert(ex.getCause.isInstanceOf[NoSuchElementException])

    cache.put(uuid1, "Two Hundred")
    assert(cache.size() === 1)
    assert(cache.get(uuid1) === "Two Hundred")

    // replace
    cache.put(uuid1, "One Hundred")
    assert(cache.get(uuid1) === "One Hundred")

    persistence.remove(uuid1)
  }
}
