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

package org.mbari.m3.panoptes.auth
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization._
import java.lang.{Long => JLong}
import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}
import java.util.Date
import javax.servlet.http.HttpServletRequest

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.typesafe.config.ConfigFactory
import org.mbari.m3.panoptes.converters.json4s

import scala.util.control.NonFatal

/**
 * To use this authentication. The client and server should both have a shared
 * secret (aka client secret). The client sends this to the server in a
 * authorization header. If the secret is correct, the server will send back
 * a JWT token that can be used to validate subsequent requests.
 *
 * {{{
 *   Client                                                                Server
 *     |-------> POST /auth: Authorization: APIKEY <client_secret>      ----->|
 *     |                                                                      |
 *     |<------- {'access_token': <token>, 'token_type': 'Bearer'}     <------|
 *     |                                                                      |
 *     |                                                                      |
 *     |-------> POST /somemethod: Authorization: Bearer <token>       ------>|
 *     |                                                                      |
 *     |<------- 200                                                   <------|
 * }}}
 * @author Brian Schlining
 * @since 2017-01-18T16:42:00
 */
class BasicJwtService extends AuthorizationService {

  private[this] val config = ConfigFactory.load()
  private[this] val issuer = config.getString("basicjwt.issuer")
  private[this] val apiKey = config.getString("basicjwt.client.secret")
  private[this] val signingSecret = config.getString("basicjwt.signing.secret")
  private[this] val algorithm = Algorithm.HMAC512(signingSecret)

  private[this] val verifier = JWT
    .require(algorithm)
    .withIssuer(issuer)
    .build()

  private def authorize(request: HttpServletRequest): Option[Authorization] =
    Option(request.getHeader("Authorization"))
      .map(parseAuthHeader)

  private def isValid(auth: Option[Authorization]): Boolean =
    //println("RUNNING JWT with auth = " + auth.getOrElse("NONE"))
    try {
      auth match {
        case None => false
        case Some(a) =>
          if (a.tokenType.equalsIgnoreCase("BEARER")) {
            val jwt = verifier.verify(a.accessToken)
            true
          } else false
      }
    } catch {
      case NonFatal(e) => false
    }

  private def parseAuthHeader(header: String): Authorization = {
    val parts = header.split("\\s")
    val tokenType = if (parts.length == 1) "undefined" else parts(0)
    val accessToken = if (parts.length == 1) parts(0) else parts(1)
    Authorization(tokenType, accessToken)
  }

  override def validateAuthorization(request: HttpServletRequest): Boolean =
    isValid(authorize(request))

  override def requestAuthorization(request: HttpServletRequest): Option[String] = {
    implicit val formats = json4s.CustomFormats
    Option(request.getHeader("Authorization"))
      .map(parseAuthHeader)
      .filter(_.tokenType.equalsIgnoreCase("APIKEY"))
      .filter(_.accessToken == apiKey)
      .map(a => {
        val now = Instant.now()
        val tomorrow = now.plus(1, ChronoUnit.DAYS)
        val iat = Date.from(now)
        val exp = Date.from(tomorrow)

        JWT
          .create()
          .withIssuer(issuer)
          .withIssuedAt(iat)
          .withExpiresAt(exp)
          .sign(algorithm)

      })
      .map(Authorization("Bearer", _).toSnakeCase) // Write snake case
      .map(a => write(a))
      // .map(s => compact(render(parse(s)))) // Write snake case
  }
}
