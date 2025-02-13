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

import io.circe.Printer
import org.mbari.panoptes.AppConfig
import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.etc.jdk.Loggers.Implicits.given
import org.mbari.panoptes.domain.*
import org.mbari.panoptes.etc.circe.CirceCodecs
import org.mbari.panoptes.etc.jwt.JwtService
import sttp.model.StatusCode
import sttp.model.headers.WWWAuthenticateChallenge
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object CustomTapirJsonCirce extends TapirJsonCirce:
    override def jsonPrinter: Printer = CirceCodecs.CustomPrinter

trait Endpoints:

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

    val baseEndpoint = addContextPath(endpoint)

    def addContextPath[A, B, C, D, E](anEndpoint: Endpoint[A, B, C, D, E]): Endpoint[A, B, C, D, E] =
        val cp = AppConfig.Http.ContextPath
        if (cp.nonEmpty && cp != "/") then
            val correctContext = if cp.startsWith("/") then cp.drop(1) else cp
            anEndpoint.in(correctContext)
        else
            anEndpoint

    val secureEndpoint: Endpoint[Option[String], Unit, ErrorMsg, Unit, Any] =
        baseEndpoint
            .securityIn(auth.bearer[Option[String]](WWWAuthenticateChallenge.bearer))
            .errorOut(
                oneOf[ErrorMsg](
                    oneOfVariant(statusCode(StatusCode.BadRequest).and(jsonBody[BadRequest])),
                    oneOfVariant(statusCode(StatusCode.NotFound).and(jsonBody[NotFound])),
                    oneOfVariant(statusCode(StatusCode.InternalServerError).and(jsonBody[ServerError])),
                    oneOfVariant(statusCode(StatusCode.Unauthorized).and(jsonBody[Unauthorized]))
                )
            )

    val openEndpoint: Endpoint[Unit, Unit, ErrorMsg, Unit, Any] =
        baseEndpoint.errorOut(
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
