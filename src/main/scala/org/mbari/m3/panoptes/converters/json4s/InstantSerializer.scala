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

import java.time.Instant
import java.time.format.DateTimeFormatter

import org.json4s._
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

/**
 * @author Brian Schlining
 * @since 2017-03-01T14:07:00
 */
case object InstantSerializer
    extends CustomSerializer[Instant](format =>
      ({
        case JString(s) => InstantSerializerSupport.parse(s).getOrElse(null)
        case JNull      => null
      }, {
        case i: Instant =>
          JString(InstantSerializerSupport.format(i))
      }))

object InstantSerializerSupport {

  private[this] val dtf = DateTimeFormatter.ISO_INSTANT
  private[this] val log = LoggerFactory.getLogger(getClass)

  def parse(s: String): Option[Instant] =
    try {
      Option(Instant.from(dtf.parse(s)))
    } catch {
      case NonFatal(e) => {
        log.info("Failed to parse: " + s)
        None
      }
    }

  def format(i: Instant): String = dtf.format(i)
}
