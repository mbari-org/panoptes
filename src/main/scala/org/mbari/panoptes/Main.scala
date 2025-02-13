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

import org.mbari.panoptes.etc.jdk.Loggers.Implicits.given
import sttp.tapir.server.vertx.VertxFutureServerOptions
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import sttp.tapir.server.vertx.{VertxFutureServerInterpreter, VertxFutureServerOptions}
import sttp.tapir.server.vertx.VertxFutureServerInterpreter.VertxFutureToScalaFuture
import io.vertx.core.VertxOptions

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.handler.BodyHandler
import org.mbari.panoptes.endpoints.{AuthorizationEndpoints, HealthEndpoints, ImageEndpoints}
import org.mbari.panoptes.etc.jdk.Loggers
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.metrics.prometheus.PrometheusMetrics

/**
 * Launches Oni
 */
object Main:

    def main(args: Array[String]): Unit =

        System.setProperty("user.timezone", "UTC")

        val s =
            """
              |  _______  _______  _        _______  _______ _________ _______  _______
              | (  ____ )(  ___  )( (    /|(  ___  )(  ____ )\__   __/(  ____ \(  ____ \
              | | (    )|| (   ) ||  \  ( || (   ) || (    )|   ) (   | (    \/| (    \/
              | | (____)|| (___) ||   \ | || |   | || (____)|   | |   | (__    | (_____
              | |  _____)|  ___  || (\ \) || |   | ||  _____)   | |   |  __)   (_____  )
              | | (      | (   ) || | \   || |   | || (         | |   | (            ) |
              | | )      | )   ( || )  \  || (___) || )         | |   | (____/\/\____) |
              | |/       |/     \||/    )_)(_______)|/          )_(   (_______/\_______)""".stripMargin + s"  v${AppConfig.Version}"

        println(s)

        val log  = System.getLogger(getClass.getName)
        val port = sys.env.get("HTTP_PORT").flatMap(_.toIntOption).getOrElse(AppConfig.Http.Port)
        log.atInfo.log(s"Starting ${AppConfig.Name} v${AppConfig.Version} on port $port")

        val serverOptions = VertxFutureServerOptions
            .customiseInterceptors
            .metricsInterceptor(Endpoints.prometheusMetrics.metricsInterceptor())
            .options

        val vertx             = Vertx.vertx(new VertxOptions().setWorkerPoolSize(AppConfig.NumberOfThreads))
        val httpServerOptions = new HttpServerOptions().setCompressionSupported(true)
        val server            = vertx.createHttpServer(httpServerOptions)
        val router            = Router.router(vertx)

        // Set the body limit to the max configured size
//        val bodyHandler = BodyHandler.create().setBodyLimit(AppConfig.Panoptes.MaxSizeBytes)
//        router.route().handler(bodyHandler)

        val interpreter       = VertxFutureServerInterpreter(serverOptions)
        Endpoints.attachRoutes(router, interpreter)

        val program = server.requestHandler(router).listen(port).asScala

        Await.result(program, Duration.Inf)
