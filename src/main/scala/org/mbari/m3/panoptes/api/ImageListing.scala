package org.mbari.m3.panoptes.api

import java.net.URI

/**
 * @author Brian Schlining
 * @since 2017-08-30T13:08:00
 */
case class ImageListing(cameraId: String, deploymentId: String, files: List[URI])
