package org.mbari.m3.panoptes.converters

import org.json4s.ext.UUIDSerializer
import org.json4s.{DefaultFormats, Formats}

/**
  * @author Brian Schlining
  * @since 2017-08-29T11:32:00
  */
package object json4s {

  val CustomFormats: Formats = DefaultFormats +
    ByteArraySerializer +
    DurationSerializer +
    InstantSerializer +
    URISerializer +
    URLSerializer +
    UUIDSerializer

}
