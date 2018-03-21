package org.mbari.m3.panoptes.converters.json4s

import java.net.{URI, URL}

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

import scala.util.Try

/**
 * @author Brian Schlining
 * @since 2017-03-01T15:12:00
 */
case object URISerializer
    extends CustomSerializer[URI](format =>
      ({
        case JString(s) => Try(new URI(s)).getOrElse(null)
        case JNull      => null
      }, {
        case x: URL => JString(x.toString)
      }))
