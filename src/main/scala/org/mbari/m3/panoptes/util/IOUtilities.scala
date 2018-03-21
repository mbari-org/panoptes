package org.mbari.m3.panoptes.util

import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.Channels

import scala.tools.nsc.interpreter.InputStream

/**
 * @author Brian Schlining
 * @since 2017-08-29T16:32:00
 */
object IOUtilities {

  def copy(in: InputStream, out: OutputStream): Unit = {

    val source = Channels.newChannel(in)
    val target = Channels.newChannel(out)

    val buffer = ByteBuffer.allocate(16 * 1024)
    while (source.read(buffer) != -1) {
      buffer.flip() // Prepare the buffer to be drained
      while (buffer.hasRemaining) {
        target.write(buffer)
      }
      buffer.clear() // Empty buffer to get ready for filling
    }

    source.close()
    target.close()

  }

}
