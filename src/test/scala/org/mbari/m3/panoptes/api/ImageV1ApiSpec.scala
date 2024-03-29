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

package org.mbari.m3.panoptes.api

import scala.concurrent.ExecutionContext
import org.mbari.m3.panoptes.util.IOUtilities
import scala.concurrent.ExecutionContextExecutor
import org.scalatra.test.Uploadable
import org.scalatra.test.BytesPart
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

/**
 * @author Brian Schlining
 * @since 2017-08-30T10:30:00
 */
class ImageV1ApiSpec extends ApiTestStack {


  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  private[this] val api = new ImageV1Api()
  private[this] val auth = new AuthorizationV1Api()

  addServlet(api, "/v1/images")
  addServlet(auth, "/v1/auth")

  private val image = getClass.getResource("/images/01_02_03_04.jpg")

  "ImageV1Api" should "POST" in {

    // First get a token
    val config = ConfigFactory.load()
    val apikey = config.getString("basicjwt.client.secret")
    var token: String = ""
    post("/v1/auth", headers = Map("Authorization" -> s"APIKEY $apikey")) {
      status should be(200)
      // println(body)
      val r = ".*\\\"(.*)\\\".*".r
      r.findAllMatchIn(body).foreach(m => token = m.group(1))
    }

    token should not be ("")

    val jpgBytes = IOUtilities.readAllBytes(image)
    submitMultipart(
      "POST",
      "/v1/images/Ventana/9999/01_02_03_04.png", 
      headers = Map("Authorization" -> s"Bearer $token"),
      files = Map("file" -> BytesPart("01_02_03_04.png", jpgBytes))
    ) {
      status should be(200)
      // println(body)
      body should include("01_02_03_04.png")
      // TOOD need to add image to multipart request

    }

  }

}

