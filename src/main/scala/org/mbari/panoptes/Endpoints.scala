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

package org.mbari.panoptes

import io.vertx.ext.web.Router
import org.mbari.panoptes.endpoints.{AuthorizationEndpoints, HealthEndpoints, ImageEndpoints}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics
import sttp.tapir.server.vertx.VertxFutureServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import org.mbari.panoptes.etc.jdk.Loggers.Implicits.given
import org.mbari.panoptes.etc.jwt.JwtService

import scala.concurrent.{ExecutionContext, Future}

object Endpoints:

    private val log = System.getLogger(getClass.getName)

    given ExecutionContext = AppConfig.DefaultExecutionContext

    val jwtService: JwtService                       = AppConfig.Basicjwt.DefaultJwtService
    val prometheusMetrics: PrometheusMetrics[Future] = PrometheusMetrics.default[Future]()
    val metricsEndpoint: ServerEndpoint[Any, Future] = prometheusMetrics.metricsEndpoint
    val authorizationEndpoints                       = new AuthorizationEndpoints(jwtService)
    val healthEndpoints                              = new HealthEndpoints()
    val imageEndpoints                               = new ImageEndpoints(jwtService)

    val endpoints: List[ServerEndpoint[Any, Future]] = List(
        authorizationEndpoints,
        healthEndpoints,
        imageEndpoints
    ).flatMap(_.allImpl)

    val docEndpoints: List[ServerEndpoint[Any, Future]] =
        SwaggerInterpreter().fromServerEndpoints(endpoints, AppConfig.Name, AppConfig.Version)

    def attachRoutes(router: Router, interpreter: VertxFutureServerInterpreter): Unit =
        endpoints.foreach { endpoint =>
            interpreter.blockingRoute(endpoint)(router)
        }
        interpreter.route(metricsEndpoint)(router)
        docEndpoints.foreach(endpoint => interpreter.route(endpoint)(router))
        router.getRoutes.forEach(r => log.atInfo.log(f"Adding route: ${r.methods()}%8s ${r.getPath}%s"))
