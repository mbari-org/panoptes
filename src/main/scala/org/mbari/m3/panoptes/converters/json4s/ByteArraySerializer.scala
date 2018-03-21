package org.mbari.m3.panoptes.converters.json4s

import java.util.Base64

import org.json4s.CustomSerializer
import org.json4s.JsonAST.{JNull, JString}

/**
 * @author Brian Schlining
 * @since 2017-03-02T11:33:00
 */
case object ByteArraySerializer
    extends CustomSerializer[Array[Byte]](format =>
      ({
        case JString(s) => Base64.getDecoder.decode(s)
        case JNull      => null
      }, {
        case x: Array[Byte] => JString(Base64.getEncoder.encodeToString(x))
      }))
