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
import java.net.URI

import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

/**
 * Archives files to some data store. Provides 2 methods.
 * One to save a file, another to construct a URI to a file
 *
 * @author Brian Schlining
 * @since 2017-08-29T14:37:00
 */
trait FileArchiver {

  /**
   * Save the data to the data archive. The Camera ID and deployment ID are
   * used to organize the files into buckets or directories. The name you
   * provide will be the name of the file.
   * @param inputStream The stream to read from
   * @param cameraId Camera ID (e.g. Ventana, Doc Ricketts)
   * @param deploymentId Some camera deployment identifier (don't use
   *                     file system unfriendly characters) e.g. 1234,
   *                     20170922
   * @param name The filename (e.g. 01_05_34_21.png)
   * @return
   */
  def save(inputStream: InputStream,
           cameraId: String,
           deploymentId: String,
           name: String): Option[URI]

  /**
   * Maps params to some URI that can be used to retrive the file resource
   * @param cameraId
   * @param deploymentId
   * @param name
   * @return
   */
  def uri(cameraId: String, deploymentId: String, name: String): URI

}

object FileArchiver {

  private[this] val config = ConfigFactory.load()

  val Instance: FileArchiver = {
    val serviceName = config.getString("panoptes.file.archiver")
    val clazz = Class.forName(serviceName)
    clazz
      .getDeclaredConstructor()
      .newInstance()
      .asInstanceOf[FileArchiver]
  }

  def save(inputStream: InputStream, cameraId: String, deploymentId: String, name: String)(
      implicit ec: ExecutionContext): Future[Option[URI]] =
    Future(Instance.save(inputStream, cameraId, deploymentId, name))

}
