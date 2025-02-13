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

package org.mbari.panoptes

import com.typesafe.config.{Config, ConfigFactory}
import org.mbari.m3.panoptes.services.DiskArchiver
import org.mbari.panoptes.etc.jwt.JwtService

import java.net.{URI, URL}
import java.nio.file.{Path, Paths}
import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.Try

object AppConfig:

    val Name: String = "panoptes"

    val Version: String =
        val v = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0-SNAPSHOT")
        if v == null then "0.0.0-SNAPSHOT" else v

    val Description: String = "Image Archiver"

    val Config: Config = ConfigFactory.load()

    val NumberOfThreads: Int = Runtime.getRuntime.availableProcessors()

    val DefaultExecutionContext: ExecutionContextExecutor =
        ExecutionContext.fromExecutor(Executors.newFixedThreadPool(NumberOfThreads))

    object Http:
        val Port: Int                 = Config.getInt("http.port")
        val StopTimeout: Int          = Config.getInt("http.stop.timeout")
        val ConnectorIdleTimeout: Int = Config.getInt("http.connector.idle.timeout")
        val Webapp: String            = Config.getString("http.webapp")
        val ContextPath: String       = Config.getString("http.context.path")

    object Basicjwt:
        val ClientSecret: String               = Config.getString("basicjwt.client.secret")
        val Issuer: String                     = Config.getString("basicjwt.issuer")
        val SigningSecret: String              = Config.getString("basicjwt.signing.secret")
        lazy val DefaultJwtService: JwtService = new JwtService(Issuer, ClientSecret, SigningSecret)

    object Panoptes:
        val FileArchiver: String          = Config.getString("panoptes.file.archiver")
        val MbariImageArchiveRoot: String = Config.getString("panoptes.mbari.image.archive.root")
        val MbariImageArchiveUrl: String  = Config.getString("panoptes.mbari.image.archive.url")
        val MaxSizeGb: Int                = Config.getInt("panoptes.max.size.gb")
        val MaxSizeBytes: Long = MaxSizeGb * 1024L * 1024L * 1024L

        def newFileArchiver(): DiskArchiver =
            val clazz = Class.forName(FileArchiver)
            clazz
                .getDeclaredConstructor()
                .newInstance()
                .asInstanceOf[DiskArchiver]

        lazy val ArchiveRoot: Path = Paths.get(MbariImageArchiveRoot)
        lazy val ArchiveUrl: URL   =
            val url = if MbariImageArchiveUrl.endsWith("/") then MbariImageArchiveUrl else MbariImageArchiveUrl + "/"
            URI.create(url).toURL
