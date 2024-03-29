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

import jakarta.servlet.http.HttpServletRequest

/**
 * Service that does not validation. All requests are valid. Useful for testing
 *
 * @author Brian Schlining
 * @since 2017-01-19T08:50:00
 */
class NoopAuthService extends AuthorizationService {

  override def requestAuthorization(request: HttpServletRequest): Option[String] = None

  override def validateAuthorization(request: HttpServletRequest): Boolean = true

}
