package org.mbari.m3.panoptes.api

import org.scalatra.Unauthorized

import scala.concurrent.ExecutionContext

/**
  * @author Brian Schlining
  * @since 2017-08-30T15:27:00
  */
class AuthorizationV1Api(implicit val executor: ExecutionContext)
  extends ApiStack {

  before() {
    contentType = "application/json"
  }

  post("/") {
    authorizationService.requestAuthorization(request) match {
      case None => halt(Unauthorized())
      case Some(s) => s
    }
  }

}

