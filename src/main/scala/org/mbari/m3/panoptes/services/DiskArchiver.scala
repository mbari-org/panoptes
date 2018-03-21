package org.mbari.m3.panoptes.services

import java.nio.file.Path

/**
 * A FileArchiver that saves files to a traditional disk.
 * @author Brian Schlining
 * @since 2017-08-30T11:01:00
 */
trait DiskArchiver extends FileArchiver {

  /**
   * Constructs the local file path used to save a file to.
   * @param cameraId
   * @param deploymentId
   * @param name
   * @return
   */
  def filepath(cameraId: String, deploymentId: String, name: String): Path

  def files(cameraId: String, deploymentId: String): List[Path]

}
