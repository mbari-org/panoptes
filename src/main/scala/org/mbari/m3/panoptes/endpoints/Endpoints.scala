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

import org.mbari.m3.panoptes.etc.circe.CirceCodecs
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import io.circe.Printer

import org.mbari.m3.panoptes.AppConfig
import java.util.concurrent.Executors

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import sttp.model.StatusCode
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.Endpoint
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint
import org.mbari.m3.panoptes.domain.{BadRequest, ErrorMsg, NotFound, ServerError, Unauthorized}
import org.mbari.m3.panoptes.etc.jdk.Loggers.Implicits.given
import org.mbari.m3.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.m3.panoptes.etc.jwt.JwtService


object CustomTapirJsonCirce extends TapirJsonCirce:
    override def jsonPrinter: Printer = CirceCodecs.CustomPrinter

trait Endpoints {

    import CustomTapirJsonCirce.*

    val log: System.Logger = System.getLogger(getClass.getName)

    def all: List[Endpoint[?, ?, ?, ?, ?]]
    def allImpl: List[ServerEndpoint[Any, Future]]

    def handleErrors[T](f: Future[T])(using ec: ExecutionContext): Future[Either[ErrorMsg, T]] =
        f.transform:
            case Success(value)     =>
                // log.atError.log(value.toString())
                Success(Right(value))
            case Failure(exception) =>
                log.atError.withCause(exception).log("Error")
                Success(Left(ServerError(exception.getMessage)))

    def handleOption[T](f: Future[Option[T]])(using ec: ExecutionContext): Future[Either[ErrorMsg, T]] =
        f.transform:
            case Success(Some(value)) => Success(Right(value))
            case Success(None)        => Success(Left(NotFound("Not found")))
            case Failure(exception)   =>
                log.atError.withCause(exception).log("Error")
                Success(Left(ServerError(exception.getMessage)))

    val secureEndpoint: Endpoint[Option[String], Unit, ErrorMsg, Unit, Any] = endpoint
        .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
        .errorOut(
            oneOf[ErrorMsg](
                oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
                oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
                oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError])),
                oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized]))
            )
        )


    val openEndpoint: Endpoint[Unit, Unit, ErrorMsg, Unit, Any] = endpoint.errorOut(
        oneOf[ErrorMsg](
            oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
            oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
            oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError]))
        )
    )

    def verify(
        jwtOpt: Option[String]
    )(using jwtService: JwtService, ec: ExecutionContext): Future[Either[Unauthorized, Unit]] =
        jwtOpt match
            case None      => Future(Left(Unauthorized("Missing token")))
            case Some(jwt) => Future(if jwtService.verify(jwt) then Right(()) else Left(Unauthorized("Invalid token")))
}
 