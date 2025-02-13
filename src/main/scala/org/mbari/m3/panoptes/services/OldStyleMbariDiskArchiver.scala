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

import scala.util.Try

/**
 * @author
 *   Brian Schlining
 * @since 2018-03-21T13:17:00
 */
class OldStyleMbariDiskArchiver extends MbariDiskArchiver:

    /**
     * The old way of archiving dives at MBARI was [platform]/images/[diveNumber]. This archiver recreates that if the
     * last `word` in the deployment id is a number. e.g. Ventana/images/2034 instead of Ventana/images/Ventana 2034
     * @param cameraId
     * @param deploymentId
     * @return
     */
    override protected def relativeFilePath(cameraId: String, deploymentId: String): String =
        val parts      = deploymentId.split("\\s+")
        val diveNumber = Try(parts.last.toInt)
            .map(s => f"$s%04d")
            .getOrElse(deploymentId)
        s"$cameraId/images/$diveNumber"
