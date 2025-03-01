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
import sttp.tapir.Endpoint
import sttp.tapir.server.ServerEndpoint
import scala.concurrent.Future
import sttp.tapir.*
import sttp.tapir.generic.auto.*
import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import CustomTapirJsonCirce.*
import org.mbari.panoptes.domain.{ErrorMsg, HealthStatus}

class HealthEndpoints(using ec: ExecutionContext) extends Endpoints:

    val healthEndpoint: Endpoint[Unit, Unit, ErrorMsg, HealthStatus, Any] =
        openEndpoint
            .get
            .in("v1" / "health")
            .out(jsonBody[HealthStatus])
            .name("health")
            .description("Health check")
            .tag("health")

    val healthEndpointImpl: ServerEndpoint[Any, Future] =
        healthEndpoint.serverLogic(_ => handleErrors(Future(HealthStatus.default)))

    override def all: List[Endpoint[?, ?, ?, ?, ?]] =
        List(healthEndpoint)

    override def allImpl: List[ServerEndpoint[Any, Future]] = List(healthEndpointImpl)
