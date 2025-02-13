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

package org.mbari.panoptes.endpoints

import sttp.client3.*
import sttp.model.StatusCode

import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.etc.circe.CirceCodecs.Extensions.*
import org.mbari.panoptes.domain.HealthStatus
import org.mbari.panoptes.etc.sdk.Futures.*

class HealthEndpointsSuite extends EndpointsSuite:

    val healthEndpoints    = new HealthEndpoints

    test("health"):

        val backendStub = newBackendStub(healthEndpoints.healthEndpointImpl)

        val request  = basicRequest.get(uri"http://test.com/v1/health")
        val response = request.send(backendStub).join
        assertEquals(response.code, StatusCode.Ok)
        response.body match
            case Left(e) => fail(e)
            case Right(r) =>
                val healthStatus = r.reify[HealthStatus]
                healthStatus match
                    case Right(hs) => // Nothing to do
                    case Left(e) => fail(e.getMessage)


