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

package org.mbari.m3.panoptes

import scala.util.Try

object AppConfig {

  val Name: String = "panoptes"

  val Version: String = {
    val v = Try(getClass.getPackage.getImplementationVersion).getOrElse("0.0.0-SNAPSHOT")
    if (v == null) "0.0.0-SNAPSHOT" else v
  }

  val Description: String = "Image Archiver"

}