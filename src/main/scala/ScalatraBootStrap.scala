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

import jakarta.servlet.ServletContext

import com.typesafe.config.ConfigFactory
import org.mbari.m3.panoptes.api.{AuthorizationV1Api, ImageV1Api}
import org.scalatra.LifeCycle
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import org.mbari.m3.panoptes.api.HealthApi
import org.mbari.m3.panoptes.AppConfig

/**
 *
 *
 * @author Brian Schlining
 * @since 2016-05-20T14:41:00
 */
class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext): Unit = {

    LoggerFactory.getLogger(getClass).info(s"Mounting ${AppConfig.Name} Servlets")
    // Optional because * is the default
    context.setInitParameter("org.scalatra.cors.allowedOrigins", "*")
    // Disables cookies, but required because browsers will not allow passing credentials to wildcard domains
    context.setInitParameter("org.scalatra.cors.allowCredentials", "false")

    implicit val ec: ExecutionContext = ExecutionContext.global
    //implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors()))

    val config = ConfigFactory.load()
    val imageV1Api = new ImageV1Api(config.getInt("panoptes.max.size.gb"))
    val authApi = new AuthorizationV1Api

    context.mount(imageV1Api, "/v1/images")
    context.mount(authApi, "/v1/auth")
    context.mount(new HealthApi, "/v1/health")

  }

}
