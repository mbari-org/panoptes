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

package org.mbari.m3.panoptes.api

import java.net.{URI, URL}
import java.time.{Duration, Instant}
import java.util.UUID

import org.json4s.Formats
import org.mbari.m3.panoptes.converters.json4s
import org.scalatra.{ContentEncodingSupport, FutureSupport, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.json._
import org.scalatra.servlet.FileUploadSupport
import org.scalatra.util.conversion.TypeConverter
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try

/**
 * @author Brian Schlining
 * @since 2017-08-29T11:40:00
 */
abstract class ApiStack
    extends ScalatraServlet
    with ApiAuthenticationSupport
    with JacksonJsonSupport
    with ContentEncodingSupport
    with FutureSupport
    with FileUploadSupport {

  protected val log: Logger = LoggerFactory.getLogger(getClass)

  protected implicit lazy val jsonFormats: Formats = json4s.CustomFormats

  protected implicit val stringToUUID: TypeConverter[String, UUID] = new TypeConverter[String, UUID] {
    override def apply(s: String): Option[UUID] = Try(UUID.fromString(s)).toOption
  }

  protected implicit val stringToInstant: TypeConverter[String, Instant] = new TypeConverter[String, Instant] {
    override def apply(s: String): Option[Instant] = Try(Instant.parse(s)).toOption
  }

  protected implicit val stringToDuration: TypeConverter[String, Duration] = new TypeConverter[String, Duration] {
    override def apply(s: String): Option[Duration] = Try(Duration.ofMillis(s.toLong)).toOption
  }

  protected implicit val stringToURI: TypeConverter[String, URI] = new TypeConverter[String, URI] {
    override def apply(s: String): Option[URI] = Try(URI.create(s)).toOption
  }

  protected implicit val stringToURL: TypeConverter[String, URL] = new TypeConverter[String, URL] {
    override def apply(s: String): Option[URL] = Try(URI.create(s).toURL()).toOption
  }

  before() {
    contentType = formats("json")
  }

}
