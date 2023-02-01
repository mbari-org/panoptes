/*
 * Copyright 2017 Monterey Bay Aquarium Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mbari.m3.panoptes.auth

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import org.mbari.m3.panoptes.converters.json4s
import org.json4s.Formats

class AuthorizationSpec extends AnyFlatSpec with Matchers {

  given formats: Formats = json4s.CustomFormats

  "Authorization" should "serialize to snake-case JSON" in {
    val auth = new Authorization("BEARER", "12345")
    val s = write(auth)
    s must include ("tokenType")
    s must include ("accessToken")
    val t = write(auth.toSnakeCase)
    t must include ("token_type")
    t must include ("access_token")
  }
  
}
