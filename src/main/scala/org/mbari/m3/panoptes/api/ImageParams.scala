package org.mbari.m3.panoptes.api

import java.net.URI

/**
  * @author Brian Schlining
  * @since 2017-08-30T12:51:00
  */
case class ImageParams(uri: String,
                       cameraId: String,
                       deploymentId: String,
                       name: String)
