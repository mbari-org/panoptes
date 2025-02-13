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

import io.circe.parser.decode
import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.domain.AuthorizationSC
import org.mbari.panoptes.etc.jwt.JwtService
import sttp.client3.*
import sttp.model.StatusCode
import org.mbari.panoptes.etc.sdk.Futures.*


class AuthorizationEndpointsSuite extends EndpointsSuite:

    given jwtService: JwtService            = new JwtService("mbari", "foo", "bar")
    val authorizationEndpoints = new AuthorizationEndpoints(jwtService)


    test("auth with valid apikey"):
        val backendStub = newBackendStub(authorizationEndpoints.authEndpointImpl)

        val response = basicRequest
            .post(uri"http://test.com/v1/auth")
            .header("Authorization", "APIKEY foo")
            .send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Ok)
                val attempt = for
                    body <- r.body
                    decoded <- decode[AuthorizationSC](body)
                yield decoded

                attempt match
                    case Left(e) => fail(e.toString)
                    case Right(auth) =>
                        assert(jwtService.verify(auth.access_token))
            )
            .join

    test("auth with invalid apikey"):

        val backendStub = newBackendStub(authorizationEndpoints.authEndpointImpl)

        val response = basicRequest
            .post(uri"http://test.com/v1/auth")
            .header("Authorization", "APIKEY bar")
            .send(backendStub)

        response
            .map(r =>
                assertEquals(r.code, StatusCode.Unauthorized)
            )

