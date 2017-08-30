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
abstract class ApiStack extends ScalatraServlet
  with ApiAuthenticationSupport
  with JacksonJsonSupport
  with ContentEncodingSupport
  with FutureSupport
  with FileUploadSupport {

  protected[this] val log: Logger = LoggerFactory.getLogger(getClass)

  protected[this] implicit lazy val jsonFormats: Formats = json4s.CustomFormats

  protected implicit val stringToUUID = new TypeConverter[String, UUID] {
    override def apply(s: String): Option[UUID] = Try(UUID.fromString(s)).toOption
  }

  protected implicit val stringToInstant = new TypeConverter[String, Instant] {
    override def apply(s: String): Option[Instant] = Try(Instant.parse(s)).toOption
  }

  protected implicit val stringToDuration = new TypeConverter[String, Duration] {
    override def apply(s: String): Option[Duration] = Try(Duration.ofMillis(s.toLong)).toOption
  }

  protected implicit val stringToURI = new TypeConverter[String, URI] {
    override def apply(s: String): Option[URI] = Try(URI.create(s)).toOption
  }

  protected implicit val stringToURL = new TypeConverter[String, URL] {
    override def apply(s: String): Option[URL] = Try(new URL(s)).toOption
  }

  before() {
    contentType = formats("json")
  }

}
