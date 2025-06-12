package controllers

import helpers.DefaultServerSpec
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

class HealthcheckControllerSpec extends DefaultServerSpec {

  "index" must {
    "return 200 with health status" in {
      val request = FakeRequest(GET, "/healthcheck")
      val response = route(app, request).get
      
      status(response) mustBe OK
      val json = contentAsJson(response)
      (json \ "status").as[String] mustBe "healthy"
      (json \ "service").as[String] mustBe "waivers"
      (json \ "version").as[String] mustBe "0.0.1"
    }
  }
}