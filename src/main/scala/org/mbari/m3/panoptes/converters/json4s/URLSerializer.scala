package org.mbari.m3.panoptes.converters.json4s

import java.net.URL

import org.json4s._

import scala.util.Try

/**
 * @author Brian Schlining
 * @since 2017-03-01T15:05:00
 */
case object URLSerializer
    extends CustomSerializer[URL](format =>
      ({
        case JString(s) => Try(new URL(s)).getOrElse(null)
        case JNull      => null
      }, {
        case x: URL => JString(x.toExternalForm)
      }))
