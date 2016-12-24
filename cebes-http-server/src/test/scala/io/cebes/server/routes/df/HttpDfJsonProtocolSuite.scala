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
 * Created by phvu on 24/12/2016.
 */

package io.cebes.server.routes.df

import java.util.UUID

import io.cebes.df.Column
import io.cebes.df.expressions.{Literal, Pow}
import io.cebes.spark.df.expressions.SparkPrimitiveExpression
import org.scalatest.FunSuite
import spray.json._
import io.cebes.server.routes.df.HttpDfJsonProtocol._

class HttpDfJsonProtocolSuite extends FunSuite {

  test("Column") {
    val col = new Column(Pow(SparkPrimitiveExpression(UUID.randomUUID(), "col_blah", None), Literal(200)))
    val colStr = col.toJson.compactPrint

    val col2 = colStr.parseJson.convertTo[Column]
    assert(col2.expr === col.expr)
  }

  test("FillNAWithMapRequest") {
    val r = FillNAWithMapRequest(UUID.randomUUID(),
      Map("a" -> 10, "b" -> 100L, "c" -> 20.0f, "d" -> 100.0, "e" -> "aaaa", "f" -> true, "g" -> false))
    val s = r.toJson.compactPrint

    val r2 = s.parseJson.convertTo[FillNAWithMapRequest]
    assert(r === r2)
  }

  test("ReplaceRequest") {
    val r1 = ReplaceRequest(UUID.randomUUID(), Array("a", "b", "c"),
      Map("x" -> "y", "aaa" -> ""))
    val s1 = r1.toJson.compactPrint
    val r2 = s1.parseJson.convertTo[ReplaceRequest]
    assert(r2.df === r1.df)
    assert(r2.cols === r1.cols)
    assert(r2.replacement("x") === "y")
    assert(r2.replacement("aaa") === "")

    val r3 = ReplaceRequest(UUID.randomUUID(), Array("a", "b", "c"),
      Map(10.0 -> 0.9, -10.0 -> 20.5))
    val s3 = r3.toJson.compactPrint
    val r4 = s3.parseJson.convertTo[ReplaceRequest]
    assert(r4.df === r3.df)
    assert(r4.cols === r3.cols)
    assert(r4.replacement(10.0) === 0.9)
    assert(r4.replacement(-10.0) === 20.5)

    val r5 = ReplaceRequest(UUID.randomUUID(), Array("a", "b", "c"),
      Map(true -> false, false -> true))
    val s5 = r5.toJson.compactPrint
    val r6 = s5.parseJson.convertTo[ReplaceRequest]
    assert(r6.df === r5.df)
    assert(r6.cols === r5.cols)
    assert(r6.replacement(true) === false)
    assert(r6.replacement(false) === true)
  }
}
