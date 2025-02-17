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

import org.junit.Assert.{assertNotNull}
import sttp.client3.*
import sttp.model.StatusCode
import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.etc.circe.CirceCodecs.Extensions.*
import org.mbari.panoptes.domain.ImageParams
import org.mbari.panoptes.etc.jwt.JwtService
import org.mbari.panoptes.etc.sdk.Futures.*

import java.lang.System.Logger.Level
import java.net.URI
import java.nio.file.{Files, Paths}

class ImageEndpointsSuite extends EndpointsSuite:

    private val apiKey = "foo"
    given jwtService: JwtService         = JwtService("mbari", apiKey, "bar")
    val imageEndpoints = new ImageEndpoints(jwtService)

    test("upload") {
        val backendStub = newBackendStub(imageEndpoints.uploadImageImpl)
        val jwtOpt = jwtService.authorize(apiKey)
        assert(jwtOpt.isDefined)
        val jwt = jwtOpt.get

        val imageUrl = getClass.getResource("/images/01_02_03_04.jpg")
        log.log(Level.DEBUG, imageUrl)
        assertNotNull(imageUrl)
        val imagePath = Paths.get(imageUrl.toURI)
        val request = basicRequest
            .post(uri"http://test.com/v1/images/camera/deployment/01_02_03_04.jpg")
            .multipartBody(multipartFile("file", imagePath.toFile).contentType("image/jpeg"))
            .auth.bearer(jwt)
        val response = request.send(backendStub).join
        assertEquals(response.code, StatusCode.Ok)
        response.body match
            case Left(e) => fail(e)
            case Right(json) =>
                json.reify[ImageParams] match
                    case Right(imageParams) =>
                        assertEquals(imageParams.cameraId, "camera")
                        assertEquals(imageParams.deploymentId, "deployment")
                        assertEquals(imageParams.name, "01_02_03_04.jpg")
                        val newImagePath = Paths.get(URI.create(imageParams.uri))
                        assert(Files.exists(newImagePath))
                    case Left(e) => fail(e.getMessage)

    }

    test("resolve") {
        runGet(imageEndpoints.resolveImageLocationImpl,
            "http://test.com/v1/images/camera/deployment/01_02_03_04.jpg",
            response => {
                assertEquals(response.code, StatusCode.Ok)
                response.body match
                    case Left(e) => fail(e)
                    case Right(json) =>
                        json.reify[ImageParams] match
                            case Right(imageParams) =>
                                assertEquals(imageParams.cameraId, "camera")
                                assertEquals(imageParams.deploymentId, "deployment")
                                assertEquals(imageParams.name, "01_02_03_04.jpg")
                                assert(imageParams.uri.endsWith("01_02_03_04.jpg"))
                            case Left(e) => fail(e.getMessage)
            }
        )
    }


