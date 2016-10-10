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
 * Created by phvu on 20/09/16.
 */

package io.cebes.server.helpers

import com.google.inject.Inject
import io.cebes.prop.Property
import io.cebes.prop.Prop

class TestProperties @Inject()
(@Prop(Property.TEST_AWS_ACCESSKEY) val awsAccessKey: String,
@Prop(Property.TEST_AWS_SECRETKEY) val awsSecretKey: String) {

  def hasS3Credentials = !awsSecretKey.isEmpty && !awsAccessKey.isEmpty
}
