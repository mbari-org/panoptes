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

package org.mbari.panoptes.util

import java.io.{InputStream, OutputStream}
import java.nio.ByteBuffer
import java.nio.channels.Channels
import scala.util.Using
import java.net.URL

/**
 * @author
 *   Brian Schlining
 * @since 2017-08-29T16:32:00
 */
object IOUtilities:

    def copy(in: InputStream, out: OutputStream): Unit =

        val source = Channels.newChannel(in)
        val target = Channels.newChannel(out)

        val buffer = ByteBuffer.allocate(16 * 1024)
        while source.read(buffer) != -1 do
            buffer.flip()  // Prepare the buffer to be drained
            while buffer.hasRemaining do target.write(buffer)
            buffer.clear() // Empty buffer to get ready for filling

        source.close()
        target.close()

    def readAllBytes(in: InputStream): Array[Byte] =
        val out = new java.io.ByteArrayOutputStream()
        copy(in, out)
        out.toByteArray

    def readAllBytes(url: URL): Array[Byte] =
        Using.resource(url.openStream()) { in =>
            readAllBytes(in)
        }
