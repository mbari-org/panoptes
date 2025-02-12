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

package org.mbari.m3.panoptes.etc.jakarta

import jakarta.servlet.*
import jakarta.servlet.http.*
import java.io.*
import java.nio.charset.StandardCharsets
import org.slf4j.LoggerFactory

class LoggingFilter extends Filter:

    private val log = LoggerFactory.getLogger(getClass)
    override def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain): Unit =
        val wrappedRequest = new CachedBodyHttpServletRequest(request.asInstanceOf[HttpServletRequest])

        // Log Headers
        val headers = wrappedRequest.getHeaderNames
        while headers.hasMoreElements do
            val name = headers.nextElement()
            log.atInfo.log(s"Header: $name -> ${wrappedRequest.getHeader(name)}")

        // Log Body
        val body = new String(wrappedRequest.getCachedBody, StandardCharsets.UTF_8)
        log.atInfo.log(s"Request Body: $body")

        chain.doFilter(wrappedRequest, response)

class CachedBodyHttpServletRequest(request: HttpServletRequest) extends HttpServletRequestWrapper(request):
    private val cachedBytes: Array[Byte] =
        val inputStream           = request.getInputStream
        val byteArrayOutputStream = new ByteArrayOutputStream()
        val buffer                = Array.ofDim[Byte](1024)
        var bytesRead             = inputStream.read(buffer)
        while bytesRead != -1 do
            byteArrayOutputStream.write(buffer, 0, bytesRead)
            bytesRead = inputStream.read(buffer)
        byteArrayOutputStream.toByteArray

    def getCachedBody: Array[Byte] = cachedBytes

    override def getInputStream: ServletInputStream = new CachedBodyServletInputStream(cachedBytes)

class CachedBodyServletInputStream(cachedBytes: Array[Byte]) extends ServletInputStream:
    private val inputStream = new ByteArrayInputStream(cachedBytes)

    override def read(): Int                                   = inputStream.read()
    override def isFinished: Boolean                           = inputStream.available() == 0
    override def isReady: Boolean                              = true
    override def setReadListener(listener: ReadListener): Unit = {}
