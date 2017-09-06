package org.mbari.m3.panoptes.api

import scala.concurrent.ExecutionContext


/**
  * @author Brian Schlining
  * @since 2017-08-30T10:30:00
  */
class ImageV1ApiSpec extends ApiTestStack {

  implicit val ec = ExecutionContext.global

  private[this] val api = new ImageV1Api()

  addServlet(api, "/v1/images")

  "ImageV1Api" should "POST" in {
    post("v1/images/Ventana/9999/01_02_03_04.png") {

    }
  }

}
