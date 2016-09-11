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
 * Created by phvu on 09/09/16.
 */

package io.cebes.server.helpers

import io.cebes.server.InjectorService
import io.cebes.server.http.HttpServer

object HttpServerTest {

  private val server = InjectorService.injector.getInstance(classOf[HttpServer])
  @volatile private var counter: Int = 0

  lazy val httpInterface = server.httpInterface

  lazy val httpPort = server.httpPort

  def register(): Unit = {
    if (counter <= 0) {
      synchronized {
        server.start()
      }
    }
    counter += 1
  }

  def unregister(): Unit = {
    counter -= 1
    if (counter <= 0) {
      synchronized {
        server.stop()
      }
    }
  }
}