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

import scala.concurrent.ExecutionContext
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.domain.{
    Authorization,
    AuthorizationSC,
    BadRequest,
    ErrorMsg,
    HealthStatus,
    NotFound,
    ServerError,
    StatusMsg,
    Unauthorized
}
import org.mbari.panoptes.etc.jwt.JwtService

import scala.concurrent.Future

class AuthorizationEndpoints(jwtService: JwtService)(using ec: ExecutionContext) extends Endpoints:

    private val log = System.getLogger(getClass.getName)

    val authEndpoint: Endpoint[String, Unit, ErrorMsg, AuthorizationSC, Any] =
        baseEndpoint
            .post
            .in("v1" / "auth")
            .securityIn(header[String]("Authorization").description("Header format is `Authorization: APIKEY <key>`"))
            .out(jsonBody[AuthorizationSC])
            .errorOut(
                oneOf[ErrorMsg](
                    oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
                    oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
                    oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError])),
                    oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized]))
                )
            )
            .name("authenticate")
            .description("Exchange an API key for a JWT. Header format is `Authorization: APIKEY <key>`")
            .tag("auth")

    val authEndpointImpl: ServerEndpoint[Any, Future] =
        authEndpoint
            .serverSecurityLogic(authHeader =>
                val parts = authHeader.split(" ")
                if parts.length != 2 || parts(0).toUpperCase() != "APIKEY" then
                    Future(Left(Unauthorized("Invalid Authorization header")))
                else
                    val apiKey = parts(1)
                    jwtService.authorize(apiKey) match
                        case None      => Future(Left(Unauthorized("Invalid API key")))
                        case Some(jwt) => Future(Right(Authorization.bearer(jwt)))
            )
            .serverLogic(bearerAuth => Unit => Future(Right(bearerAuth.toSnakeCase)))

    override val all: List[Endpoint[?, ?, ?, ?, ?]]         = List(authEndpoint)
    override val allImpl: List[ServerEndpoint[Any, Future]] =
        List(authEndpointImpl)
