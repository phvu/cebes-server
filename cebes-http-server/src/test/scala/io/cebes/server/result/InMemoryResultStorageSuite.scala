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
 * Created by phvu on 30/11/2016.
 */

package io.cebes.server.result

import java.util.UUID

import io.cebes.server.helpers.TestInjector
import io.cebes.server.models.{RequestStatuses, SerializableResult}
import org.scalatest.FunSuite
import spray.json._

class InMemoryResultStorageSuite extends FunSuite {

  test("save and get") {
    val jdbcStorage = TestInjector.instance(classOf[InMemoryResultStorage])

    val requestId = UUID.randomUUID()
    assert(jdbcStorage.get(requestId).isEmpty)

    jdbcStorage.save(SerializableResult(requestId, RequestStatuses.SCHEDULED,
      Some("""{ "some": "JSON source" }""".parseJson), Some("""{ "some": "request" }""".parseJson)))
    val result = jdbcStorage.get(requestId)
    assert(result.nonEmpty)
    assert(result.get.status === RequestStatuses.SCHEDULED)
    assert(result.get.requestId === requestId)
    assert(result.get.response.nonEmpty)
    assert(result.get.response.get.prettyPrint.length > 0)
    assert(result.get.request.nonEmpty)

    // replace
    jdbcStorage.save(SerializableResult(requestId, RequestStatuses.FAILED, None, None))
    val result2 = jdbcStorage.get(requestId)
    assert(result2.nonEmpty)
    assert(result2.get.status === RequestStatuses.FAILED)
    assert(result2.get.requestId === requestId)
    assert(result2.get.response.isEmpty)
    assert(result2.get.request.isEmpty)
  }

  test("empty json response") {
    val jdbcStorage = TestInjector.instance(classOf[JdbcResultStorage])

    val requestId = UUID.randomUUID()
    assert(jdbcStorage.get(requestId).isEmpty)

    jdbcStorage.save(SerializableResult(requestId, RequestStatuses.SCHEDULED, None, None))
    val result = jdbcStorage.get(requestId)
    assert(result.nonEmpty)
    assert(result.get.status === RequestStatuses.SCHEDULED)
    assert(result.get.requestId === requestId)
    assert(result.get.response.isEmpty)
  }

}
