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

/**
 * @author Brian Schlining
 * @since 2017-08-29T10:48:00
 */
case class Authorization(tokenType: String, accessToken: String):
  
    def toSnakeCase: AuthorizationSnakeCase = AuthorizationSnakeCase(tokenType, accessToken)

/**
  * Snake case version of Authorization
  *
  * @param token_type
  * @param access_token
  */
case class AuthorizationSnakeCase(token_type: String, access_token: String)
