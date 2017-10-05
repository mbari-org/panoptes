package org.mbari.m3.panoptes.services

import java.net.{URI, URL}
import java.nio.file.{DirectoryStream, Files, Path, Paths}

import com.google.common.net.UrlEscapers
import com.typesafe.config.{Config, ConfigFactory}
import org.mbari.m3.panoptes.util.IOUtilities
import org.slf4j.LoggerFactory

import scala.tools.nsc.interpreter.InputStream
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal
import scala.collection.JavaConverters._

/**
  * @author Brian Schlining
  * @since 2017-08-29T11:45:00
  */
class MbariDiskArchiver extends DiskArchiver {

  private[this] val log = LoggerFactory.getLogger(getClass)
  private[this] val config = ConfigFactory.load()
  private[this] val archiveRoot: Path =
    Paths.get(config.getString("panoptes.mbari.image.archive.root"))
  private[this] val archiveUrl: String = {
    val url = config.getString("panoptes.mbari.image.archive.url")
    if (!url.endsWith("/")) url + "/" else url
  }


  private def relativeFilePath(cameraId: String, deploymentId: String): String =
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

    val ok = if (Files.exists(parent)) true
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
      }
      catch {
        case NonFatal(e) =>
          log.error(s"Failed to write to $path", e)
          None
      }
    }
    else None
  }

  override def uri(cameraId: String, deploymentId: String, name: String) = {
    val uriPath = s"$archiveUrl${relativeFilePath(cameraId, deploymentId)}/$name"
    val uriEscaped = UrlEscapers.urlPathSegmentEscaper().escape(uriPath)
    new URL(uriEscaped).toURI
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
