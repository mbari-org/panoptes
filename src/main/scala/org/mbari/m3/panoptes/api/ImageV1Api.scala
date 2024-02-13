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

import java.io.ByteArrayOutputStream

import org.mbari.m3.panoptes.services.{DiskArchiver, FileArchiver}
import org.mbari.m3.panoptes.util.IOUtilities
import org.scalatra.BadRequest
import org.scalatra.servlet.MultipartConfig

import scala.concurrent.ExecutionContext
import scala.util.control.NonFatal

/**
  * @author Brian Schlining
  * @since 2017-08-29T11:44:00
  */
class ImageV1Api(maxFileSizeGB: Int = 3)(implicit val executor: ExecutionContext) extends ApiStack {

  val fileArchiver = FileArchiver.Instance
  private val log = org.slf4j.LoggerFactory.getLogger(getClass)

  // Configure Max upload size
  configureMultipartHandling(
    MultipartConfig(maxFileSize = Some(maxFileSizeGB * 1024 * 1024 * 1024))
  )

  post("/:camera_id/:deployment_id/:name") {
    validateRequest()
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest(body = """{"error": "A camera_id path parameter is required"}""")))
    val deploymentId = params
      .get("deployment_id")
      .getOrElse(
        halt(BadRequest(body = """{"error": "A deployment_id path parameter is required"}"""))
      )
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest(body = """{"error": "A name path parameter is required"}""")))
    fileParams.get("file") match {
      case Some(file) =>
        val inputStream = file.getInputStream
        val f = FileArchiver
          .save(inputStream, cameraId, deploymentId, name)
          .map({
            case None    => halt(BadRequest(body = """{"error": "Unable to complete your request"}"""))
            case Some(u) => ImageParams(u.toString, cameraId, deploymentId, name)
          })
        f.onComplete(t => 
          inputStream.close()
          t match {
            case scala.util.Success(v) => log.atDebug().log(s"Archived image and returned: $v")
            case scala.util.Failure(e) =>
              log.atError().setCause(e).log(s"Failed to archive image: ${e.getMessage}")
              halt(BadRequest(body = s"""{"error": "An error occurred: ${e.getMessage}"}"""))
          }
        )
        // f.onComplete(_ => inputStream.close())
        f
      case None =>
        halt(BadRequest(body = """{"error": "No file was attached"}"""))
    }
  }

  get("/:camera_id/:deployment_id/:name") {
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest(body = """{"error": "A camera_id path parameter is required"}""")))
    val deploymentId = params
      .get("deployment_id")
      .getOrElse(
        halt(BadRequest(body = """{"error": "A deployment_id path parameter is required"}"""))
      )
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest(body = """{"error": "A name path parameter is required"}""")))
    val u = fileArchiver.uri(cameraId, deploymentId, name)
    ImageParams(u.toString, cameraId, deploymentId, name)
  }

//  get("/:camera_id/:deployment_id") {
//    val cameraId = params.get("camera_id")
//      .getOrElse(halt(BadRequest(reason = "A camera_id path parameter is required")))
//    val deploymentId = params.get("deployment_id")
//      .getOrElse(halt(BadRequest(reason = "A deployment_id path parameter is required")))
//
//    fileArchiver match {
//      case d: DiskArchiver =>
//        val paths = d.files(cameraId, deploymentId)
//        ImageListing(cameraId, deploymentId, )
//    }
//
//  }

  get("/download/:camera_id/:deployment_id/:name") {
    val cameraId = params
      .get("camera_id")
      .getOrElse(halt(BadRequest(body = """{"error": "A camera_id path parameter is required"}""")))
    val deploymentId = params
      .get("deployment_id")
      .getOrElse(
        halt(BadRequest(body = """{"error": "A deployment_id path parameter is required"}"""))
      )
    val name = params
      .get("name")
      .getOrElse(halt(BadRequest(body = """{"error": "A name path parameter is required"}""")))
    fileArchiver match {
      case d: DiskArchiver => d.filepath(cameraId, deploymentId, name).toFile
      case f: FileArchiver =>
        val uri = f.uri(cameraId, deploymentId, name)
        try {
          val url = uri.toURL
          val in  = url.openStream()
          val out = new ByteArrayOutputStream()
          IOUtilities.copy(in, out)
          in.close()
          val array = out.toByteArray
          out.close()
          array
        }
        catch {
          case NonFatal(e) =>
            halt(BadRequest(body = s"""{"error": "An error occurred: ${e.getMessage}"}"""))
        }
    }
  }

}
