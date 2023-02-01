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

/**
 * @author Brian Schlining
 * @since 2017-08-30T10:30:00
 */
class ImageV1ApiSpec extends ApiTestStack {

  implicit val ec: ExecutionContext = ExecutionContext.global

  private[this] val api: ImageV1Api = new ImageV1Api()

  addServlet(api, "/v1/images")

  "ImageV1Api" should "POST" in {
    post("/v1/images/Ventana/9999/01_02_03_04.png") {
      status should be(200)
    }
  }

}
