package org.mbari.m3.panoptes.api

import org.mbari.m3.panoptes.services.FileArchiver
import org.scalatra.BadRequest
import org.scalatra.servlet.MultipartConfig

import scala.concurrent.ExecutionContext

/**
  * @author Brian Schlining
  * @since 2017-08-29T11:44:00
  */
class ImageV1Api(implicit val executor: ExecutionContext) extends ApiStack {

  // Max upload size is 6MB
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(6*1024*1024)))

  val fileArchiver = FileArchiver.service

  post("/:camera_id/:deployment_id/:name") {
    validateRequest()
    val cameraId = params.get("camera_id")
      .getOrElse(halt(BadRequest(reason = "A camera_id path parameter is required")))
    val deploymentId = params.get("deployment_id")
      .getOrElse(halt(BadRequest(reason = "A deployment_id path parameter is required")))
    val name = params.get("name")
      .getOrElse(halt(BadRequest(reason = "A name path parameter is required")))
    fileParams.get("file") match {
      case Some(file) =>
        val inputStream = file.getInputStream

      case None =>
        halt(BadRequest(reason = "No file was attached"))
    }
  }

  get("/:camera_id/:deployment_id/:name") {

  }

}
