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

package org.mbari.m3.panoptes.converters.json4s

import java.net.URL

import org.json4s._

import scala.util.Try
import java.net.URI

/**
 * @author Brian Schlining
 * @since 2017-03-01T15:05:00
 */
case object URLSerializer
    extends CustomSerializer[URL](format =>
      ({
        case JString(s) => Try(URI.create(s).toURL).getOrElse(null)
        case JNull      => null
      }, {
        case x: URL => JString(x.toExternalForm)
      }))
