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

package org.mbari.panoptes.domain

import org.mbari.panoptes.etc.circe.CirceCodecs.Implicits.given
import org.mbari.panoptes.etc.circe.CirceCodecs.Extensions.*
import org.mbari.panoptes.domain.{Authorization, AuthorizationSC}


class AuthorizationSuite extends munit.FunSuite:

    test("stringify camelCase") {
        val auth = new Authorization("BEARER", "12345")
        val json = auth.stringify
        assert(json.contains("tokenType"))
        assert(json.contains("accessToken"))
    }

    test("stringify snake_case") {
        val auth = AuthorizationSC("BEARER", "12345")
        val json = auth.stringify
        assert(json.contains("token_type"))
        assert(json.contains("access_token"))
    }


    test("reify camelCase") {
        val json = """{"tokenType":"BEARER","accessToken":"12345"}"""
        json.reify[Authorization] match
            case Right(a) =>
                assert(a.tokenType == "BEARER")
                assert(a.accessToken == "12345")
            case Left(e) => fail(e.getMessage)
    }

    test("reify snake_case") {
        val json = """{"token_type":"BEARER","access_token":"12345"}"""
        json.reify[AuthorizationSC] match
            case Right(a) =>
                assert(a.token_type == "BEARER")
                assert(a.access_token == "12345")
            case Left(e) => fail(e.getMessage)
    }

    test("reify snake_case as camelCase") {
        val auth = AuthorizationSC("BEARER", "123457")
        val json = auth.stringify
        assert(json.contains("token_type"))
        assert(json.contains("access_token"))

        json.reify[Authorization] match
            case Right(a) =>
                assert(a.tokenType == "BEARER")
                assert(a.accessToken == "123457")
            case Left(e) => fail(e.getMessage)
    }
