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

package org.mbari.m3.panoptes.endpoints

import scala.concurrent.ExecutionContext
import sttp.tapir.Endpoint
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.io.File
import sttp.model.Part
import org.mbari.m3.panoptes.etc.jwt.JwtService
import org.mbari.m3.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.m3.panoptes.domain.{
    BadRequest,
    ErrorMsg,
    HealthStatus,
    ImageParams,
    NotFound,
    ServerError,
    StatusMsg,
    Unauthorized
}
import org.mbari.m3.panoptes.services.FileArchiver
import sttp.tapir.EndpointIO.annotations.body

import scala.concurrent.Future

class ImageEndpoints(jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints:

    private val log                   = System.getLogger(getClass().getName())
    given givenJwtService: JwtService = jwtService

    case class ImageUpload(file: Part[File])

    val uploadImage: Endpoint[Option[String], (String, String, String, ImageUpload), ErrorMsg, ImageParams, Any] =
        secureEndpoint
            .post
            .in("v1" / "image" / path[String]("camera_id") / path[String]("deployment_id") / path[String]("name"))
            .in(multipartBody[ImageUpload])
            .out(jsonBody[ImageParams])
            .name("uploadImage")
            .description("Upload an image")
            .tag("image")

    val uploadImageImpl =
        uploadImage
            .serverSecurityLogic(jwtOpt => verify(jwtOpt))
            .serverLogic(_ =>
                (cameraId, deploymentId, filename, imageUpload) =>
                    val filePart = imageUpload.file
                    val file     = filePart.body.toPath
                    handleErrors(
                        FileArchiver
                            .save(file, cameraId, deploymentId, filename)
                            .map(uri => ImageParams(uri.toString, cameraId, deploymentId, filename))
                    )
            )

    val resolveImageLocation: Endpoint[Unit, (String, String, String), ErrorMsg, ImageParams, Any] =
        openEndpoint
            .get
            .in("v1" / "image" / path[String]("camera_id") / path[String]("deployment_id") / path[String]("name"))
            .out(jsonBody[ImageParams])
            .name("resolveImageLocation")
            .description("Resolve image location")
            .tag("image")

    val resolveImageLocationImpl =
        resolveImageLocation
            .serverLogic(
                (cameraId, deploymentId, filename) =>
                    handleOption(
                        Future(
                            Some(
                                ImageParams(
                                    FileArchiver.Instance.uri(cameraId, deploymentId, filename).toString,
                                    cameraId,
                                    deploymentId,
                                    filename
                                )
                            )
                        )
                    )
            )

    val downloadImage =
        openEndpoint
            .get
            .in(
                "v1" / "image" / "download" / path[String]("camera_id") / path[String]("deployment_id") / path[String](
                    "name"
                )
            )
            .out(byteArrayBody)
            .errorOut(
                oneOf[ErrorMsg](
                    oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
                    oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
                    oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError]))
                )
            )


    override val all: List[Endpoint[?, ?, ?, ?, ?]] = List(uploadImage, resolveImageLocation)
    override val allImpl: List[ServerEndpoint[Any, Future]] = List(uploadImageImpl, resolveImageLocationImpl)