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

package org.mbari.panoptes.etc.circe

import java.util.HexFormat
import io.circe.{parser, Decoder, Encoder, Json, ParsingFailure, Printer}
import scala.util.Try
import java.net.{URI, URL}
import java.time.Duration
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.mbari.panoptes.domain.{
    Authorization,
    AuthorizationSC,
    BadRequest,
    ErrorMsg,
    HealthStatus,
    ImageParams,
    NotFound,
    ServerError,
    StatusMsg,
    Unauthorized
}

object CirceCodecs:

    val CustomPrinter: Printer = Printer(
        dropNullValues = true,
        indent = ""
    )

    object Implicits:

        private val hex = HexFormat.of()

        given Encoder[Array[Byte]] = new Encoder[Array[Byte]]:
            final def apply(xs: Array[Byte]): Json =
                Json.fromString(hex.formatHex(xs))
        given Decoder[Array[Byte]] = Decoder
            .decodeString
            .emapTry(str => Try(hex.parseHex(str)))

        given Decoder[URL] = Decoder
            .decodeString
            .emapTry(str => Try(URI.create(str).toURL()))
        given Encoder[URL] = Encoder
            .encodeString
            .contramap(_.toString)

        given uriDecoder: Decoder[URI] = Decoder
            .decodeString
            .emapTry(s => Try(URI.create(s)))
        given uriEncoder: Encoder[URI] = Encoder
            .encodeString
            .contramap[URI](_.toString)

        given Decoder[Duration] = Decoder
            .decodeLong
            .emapTry(lng => Try(Duration.ofMillis(lng)))
        given Encoder[Duration] = Encoder
            .encodeLong
            .contramap(_.toMillis)

        given authorizationScDecoder: Decoder[AuthorizationSC] = deriveDecoder

        given authorizationScEncoder: Encoder[AuthorizationSC] = deriveEncoder

        private val authorizationCcDecoder: Decoder[Authorization] = deriveDecoder

        given authorizationEncoder: Encoder[Authorization] = deriveEncoder

        given authorizationDecoder: Decoder[Authorization] =
            authorizationCcDecoder or authorizationScDecoder.map(_.toCamelCase)

        given Decoder[ImageParams] = deriveDecoder
        given Encoder[ImageParams] = deriveEncoder

        given Decoder[ErrorMsg] = deriveDecoder
        given Encoder[ErrorMsg] = deriveEncoder

        given Decoder[BadRequest] = deriveDecoder
        given Encoder[BadRequest] = deriveEncoder

        given Decoder[HealthStatus] = deriveDecoder
        given Encoder[HealthStatus] = deriveEncoder

        given Decoder[StatusMsg] = deriveDecoder
        given Encoder[StatusMsg] = deriveEncoder

        given Decoder[NotFound] = deriveDecoder
        given Encoder[NotFound] = deriveEncoder

        given Decoder[ServerError] = deriveDecoder
        given Encoder[ServerError] = deriveEncoder

        given Decoder[Unauthorized] = deriveDecoder
        given Encoder[Unauthorized] = deriveEncoder

    object Extensions:

        /**
         * Convert a circe Json object to a JSON string
         *
         * @param value
         *   Any value with an implicit circe coder in scope
         */
        extension (json: Json) def stringify: String = CustomPrinter.print(json)

        /**
         * Convert an object to a JSON string
         *
         * @param value
         *   Any value with an implicit circe coder in scope
         */
        extension [T: Encoder](value: T)
            def stringify: String = Encoder[T]
                .apply(value)
                .deepDropNullValues
                .stringify

        extension [T: Decoder](jsonString: String) def toJson: Either[ParsingFailure, Json] = parser.parse(jsonString);

        extension (jsonString: String)
            def reify[T: Decoder]: Either[io.circe.Error, T] =
                for
                    json   <- jsonString.toJson
                    result <- Decoder[T].apply(json.hcursor)
                yield result
