package org.mbari.m3.panoptes.converters.json4s

import java.time.Duration

import org.json4s._

/**
 * @author Brian Schlining
 * @since 2017-03-01T14:57:00
 */
case object DurationSerializer
    extends CustomSerializer[Duration](format =>
      ({
        case JInt(s) => Duration.ofMillis(s.toLong)
        case JNull   => null
      }, {
        case x: Duration => JInt(x.toMillis)
      }))
