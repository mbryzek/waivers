package controllers

import helpers.{DefaultServerSpec, MockApiClient, DatabaseHelpers}
import models.internal._
import play.api.libs.json._
import play.api.test.Helpers._
import play.api.test._

class WaiversControllerSpec extends DefaultServerSpec 
  with MockApiClient 
  with DatabaseHelpers {

  "getProject" must {
    "return 200 with project data when project exists" in {
      val project = createProject(name = "Test Project", slug = "test-project")
      
      val request = FakeRequest(GET, s"/projects/${project.slug}")
      val response = route(app, request).get
      
      status(response) mustBe OK
      val json = contentAsJson(response)
      (json \ "id").as[String] mustBe project.id
      (json \ "name").as[String] mustBe "Test Project"
      (json \ "slug").as[String] mustBe "test-project"
    }

    "return 404 when project not found" in {
      val request = FakeRequest(GET, "/projects/nonexistent")
      val response = route(app, request).get
      
      status(response) mustBe NOT_FOUND
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "project_not_found"
      (json \ "message").as[String] must include("nonexistent")
    }
  }

  "getCurrentWaiver" must {
    "return 200 with waiver data when current waiver exists" in {
      val project = createProject(slug = "test-project")
      val waiver = createWaiver(project.id, title = "Test Waiver", content = "Test content", isCurrent = true)
      
      val request = FakeRequest(GET, s"/projects/${project.slug}/waiver")
      val response = route(app, request).get
      
      status(response) mustBe OK
      val json = contentAsJson(response)
      (json \ "id").as[String] mustBe waiver.id
      (json \ "title").as[String] mustBe "Test Waiver"
      (json \ "content").as[String] mustBe "Test content"
      (json \ "isCurrent").as[Boolean] mustBe true
    }

    "return 404 when project not found" in {
      val request = FakeRequest(GET, "/projects/nonexistent/waiver")
      val response = route(app, request).get
      
      status(response) mustBe NOT_FOUND
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "project_not_found"
    }

    "return 404 when no current waiver exists" in {
      val project = createProject(slug = "test-project")
      // Create waiver but mark as not current
      createWaiver(project.id, isCurrent = false)
      
      val request = FakeRequest(GET, s"/projects/${project.slug}/waiver")
      val response = route(app, request).get
      
      status(response) mustBe NOT_FOUND
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "waiver_not_found"
    }
  }

  "createSignature" must {
    "return 201 with signature data when valid form submitted" in {
      val project = createProject(slug = "test-project")
      val waiver = createWaiver(project.id, isCurrent = true)
      
      val formData = Json.obj(
        "firstName" -> "John",
        "lastName" -> "Doe", 
        "email" -> "john.doe@example.com",
        "phone" -> "555-1234"
      )
      
      val request = FakeRequest(POST, s"/projects/${project.slug}/signatures")
        .withJsonBody(formData)
        .withHeaders("Content-Type" -> "application/json")
      
      val response = route(app, request).get
      
      status(response) mustBe CREATED
      val json = contentAsJson(response)
      (json \ "userId").asOpt[String] must be(defined)
      (json \ "waiverId").as[String] mustBe waiver.id
      (json \ "status").as[String] mustBe "pending"
    }

    "return 400 when form validation fails" in {
      val project = createProject(slug = "test-project")
      createWaiver(project.id, isCurrent = true)
      
      val invalidFormData = Json.obj(
        "firstName" -> "",  // Empty first name
        "lastName" -> "Doe",
        "email" -> "invalid-email",  // Invalid email
        "phone" -> "123"  // Invalid phone
      )
      
      val request = FakeRequest(POST, s"/projects/${project.slug}/signatures")
        .withJsonBody(invalidFormData)
        .withHeaders("Content-Type" -> "application/json")
      
      val response = route(app, request).get
      
      status(response) mustBe BAD_REQUEST
      val json = contentAsJson(response)
      json.as[JsArray].value.length must be > 0
    }

    "return 404 when project not found" in {
      val formData = Json.obj(
        "firstName" -> "John",
        "lastName" -> "Doe",
        "email" -> "john.doe@example.com"
      )
      
      val request = FakeRequest(POST, "/projects/nonexistent/signatures")
        .withJsonBody(formData)
        .withHeaders("Content-Type" -> "application/json")
      
      val response = route(app, request).get
      
      status(response) mustBe NOT_FOUND
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "project_not_found"
    }

    "return 500 when no current waiver exists" in {
      val project = createProject(slug = "test-project")
      // Don't create any waiver
      
      val formData = Json.obj(
        "firstName" -> "John",
        "lastName" -> "Doe",
        "email" -> "john.doe@example.com"
      )
      
      val request = FakeRequest(POST, s"/projects/${project.slug}/signatures")
        .withJsonBody(formData)
        .withHeaders("Content-Type" -> "application/json")
      
      val response = route(app, request).get
      
      status(response) mustBe INTERNAL_SERVER_ERROR
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "signature_creation_failed"
    }
  }

  "getSignature" must {
    "return 200 with signature data when signature exists" in {
      val project = createProject()
      val waiver = createWaiver(project.id)
      val user = createUser()
      val signature = createSignature(user.id, waiver.id)
      
      val request = FakeRequest(GET, s"/signatures/${signature.id}")
      val response = route(app, request).get
      
      status(response) mustBe OK
      val json = contentAsJson(response)
      (json \ "id").as[String] mustBe signature.id
      (json \ "userId").as[String] mustBe user.id
      (json \ "waiverId").as[String] mustBe waiver.id
      (json \ "status").as[String] mustBe "pending"
    }

    "return 404 when signature not found" in {
      val request = FakeRequest(GET, "/signatures/nonexistent")
      val response = route(app, request).get
      
      status(response) mustBe NOT_FOUND
      val json = contentAsJson(response)
      (json \ "code").as[String] mustBe "signature_not_found"
    }
  }
}