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

import io.circe.*
import io.circe.parser.*
import org.mbari.panoptes.AppConfig
import sttp.client3.*
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.stub.TapirStubInterpreter
import sttp.tapir.server.interceptor.exception.ExceptionHandler
import sttp.tapir.server.interceptor.CustomiseInterceptors
import sttp.model.StatusCode
import sttp.client3.SttpBackend
import sttp.tapir.server.model.ValuedEndpointOutput
import org.mbari.panoptes.etc.jdk.Loggers.Implicits.given
import sttp.tapir.server.vertx.VertxFutureServerOptions
import org.mbari.panoptes.etc.sdk.Futures.*

import scala.concurrent.{ExecutionContext, Future}

trait EndpointsSuite extends munit.FunSuite:

    given ExecutionContext         = AppConfig.DefaultExecutionContext
    protected val log: System.Logger = System.getLogger(getClass.getName)

    def runDelete(
                     ep: ServerEndpoint[Any, Future],
                     uri: String,
                     assertions: Response[Either[String, String]] => Unit,
                     jwt: Option[String] = None
                 ): Unit =
        val backendStub = newBackendStub(ep)
        val u           = uri"$uri"
        val request     = jwt match
            case None         => basicRequest.delete(u)
            case Some(bearer) =>
                basicRequest
                    .delete(u)
                    .auth
                    .bearer(bearer)
        val response    = request.send(backendStub).join
        assertions(response)

    def runPut(
                  ep: ServerEndpoint[Any, Future],
                  uri: String,
                  body: String,
                  assertions: Response[Either[String, String]] => Unit,
                  jwt: Option[String] = None
              ): Unit =
        val backendStub = newBackendStub(ep)
        val u           = uri"$uri"
        val request     = jwt match
            case None         => basicRequest.put(u).body(body)
            case Some(bearer) =>
                basicRequest
                    .put(u)
                    .body(body)
                    .auth
                    .bearer(bearer)
        val response    = request.send(backendStub).join
        assertions(response)

    def runPost(
                   ep: ServerEndpoint[Any, Future],
                   uri: String,
                   body: String,
                   assertions: Response[Either[String, String]] => Unit,
                   jwt: Option[String] = None,
                   contentType: String = "application/json"
               ): Unit =
        val backendStub = newBackendStub(ep)
        val u           = uri"$uri"
        val request     = jwt match
            case None         => basicRequest.post(u).body(body).header("Content-Type", contentType)
            case Some(bearer) =>
                basicRequest
                    .post(u)
                    .header("Content-Type", contentType)
                    .body(body)
                    .auth
                    .bearer(bearer)

        val response = request.send(backendStub).join
        assertions(response)

    def runGet(
                  ep: ServerEndpoint[Any, Future],
                  uri: String,
                  assertions: Response[Either[String, String]] => Unit,
                  jwt: Option[String] = None
              ): Unit =
        val backendStub = newBackendStub(ep)
        val u           = uri"$uri"
        val request     = jwt match
            case None         => basicRequest.get(u)
            case Some(bearer) =>
                basicRequest
                    .get(u)
                    .auth
                    .bearer(bearer)
        log.atDebug.log("--REQUEST: " + request)
        val response    = request.send(backendStub).join
        log.atDebug.log("--RESPONSE: " + response)
        assertions(response)

    def checkResponse[T: Decoder](responseBody: Either[String, String]): T =
        responseBody match
            case Left(e)     => fail(e)
            case Right(json) =>
                decode[T](json) match
                    case Left(error)  => fail(error.getLocalizedMessage)
                    case Right(value) => value

    /**
     * Creates a stubbed backend for testing endpoints. Adds exception logging to the stub.
     *
     * @param serverEndpoint
     * @return
     */
    def newBackendStub(serverEndpoint: ServerEndpoint[Any, Future]): SttpBackend[Future, Any] =
        // --- START: This block adds exception logging to the stub
        val exceptionHandler = ExceptionHandler.pure[Future](ctx =>
            Some(
                ValuedEndpointOutput(
                    sttp.tapir.stringBody.and(sttp.tapir.statusCode),
                    (s"failed due to ${ctx.e.getMessage}", StatusCode.InternalServerError)
                )
            )
        )

        val customOptions: CustomiseInterceptors[Future, VertxFutureServerOptions] =
            import scala.concurrent.ExecutionContext.Implicits.global
            VertxFutureServerOptions
                .customiseInterceptors
                .exceptionHandler(exceptionHandler)
        // --- END: This block adds exception logging to the stub
        TapirStubInterpreter(customOptions, SttpBackendStub.asynchronousFuture)
            .whenServerEndpointRunLogic(serverEndpoint)
            .backend()

