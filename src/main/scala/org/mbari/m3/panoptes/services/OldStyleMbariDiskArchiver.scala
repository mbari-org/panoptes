package org.mbari.m3.panoptes.services

import scala.util.Try

/**
 * @author Brian Schlining
 * @since 2018-03-21T13:17:00
 */
class OldStyleMbariDiskArchiver extends MbariDiskArchiver{


  /**
    * The old way of archiving dives at MBARI was [platform]/images/[diveNumber].
    * This archiver recreates that if the last `word` in the deployment id is a
    * number. e.g. Ventana/images/2034 instead of Ventana/images/Ventana 2034
    * @param cameraId
    * @param deploymentId
    * @return
    */
  override protected def relativeFilePath(cameraId: String, deploymentId: String): String = {
    val parts = deploymentId.split("\\s+")
    val diveNumber = Try(parts.last.toInt).getOrElse(deploymentId)
    s"$cameraId/images/$diveNumber"
  }

}
