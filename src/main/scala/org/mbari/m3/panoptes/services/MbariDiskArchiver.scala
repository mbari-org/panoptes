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

package org.mbari.m3.panoptes.services

import java.io.InputStream
import java.net.{URI, URL}
import java.nio.file.{Files, Path, Paths}

import com.typesafe.config.ConfigFactory
import org.mbari.m3.panoptes.util.IOUtilities
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal
import scala.jdk.CollectionConverters._

/**
 * @author Brian Schlining
 * @since 2017-08-29T11:45:00
 */
class MbariDiskArchiver extends DiskArchiver {

  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val config = ConfigFactory.load()
  protected[this] val archiveRoot: Path =
    Paths.get(config.getString("panoptes.mbari.image.archive.root"))
  private[this] val archiveUrl: String = {
    val url = config.getString("panoptes.mbari.image.archive.url")
    if (!url.endsWith("/")) url + "/" else url
  }

  protected def relativeFilePath(cameraId: String, deploymentId: String): String =
    s"$cameraId/images/$deploymentId"

  /**
   * Save a file to a data store
   * @param inputStream The input stream to the image file to be saved
   * @param cameraId The camera id (e.g. Ventana, i2Map, Doc Ricketts
   * @param deploymentId The deployment id
   * @param name
   * @return
   */
  def save(inputStream: InputStream,
           cameraId: String,
           deploymentId: String,
           name: String): Option[URI] = {

    val path = filepath(cameraId, deploymentId, name)
    val parent = path.getParent

    val ok =
      if (Files.exists(parent)) true
      else {
        Try(Files.createDirectories(parent)) match {
          case Success(v) => true
          case Failure(e) =>
            log.error(s"Failed to create directory: $parent", e)
            false
        }
      }

    if (ok) {
      try {
        val outputStream = Files.newOutputStream(path)
        IOUtilities.copy(inputStream, outputStream)
        outputStream.close()
        Some(uri(cameraId, deploymentId, name))
      } catch {
        case NonFatal(e) =>
          log.error(s"Failed to write to $path", e)
          None
      }
    } else None
  }

  override def uri(cameraId: String, deploymentId: String, name: String): URI = {
    val uriPath = s"$archiveUrl${relativeFilePath(cameraId, deploymentId)}/$name"
    val uriEscaped = uriPath.replace(" ", "%20")
    URI.create(uriEscaped)
  }

  override def filepath(cameraId: String, deploymentId: String, name: String): Path = {
    val parent = Paths.get(archiveRoot.toString, relativeFilePath(cameraId, deploymentId))
    Paths.get(parent.toString, name)
  }

  override def files(cameraId: String, deploymentId: String): List[Path] = {
    val parent = Paths.get(archiveRoot.toString, relativeFilePath(cameraId, deploymentId))
    val stream = Files.newDirectoryStream(parent)
    val paths = stream.asScala.toList
    stream.close()
    paths
  }

}
