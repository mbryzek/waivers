package controllers

import helpers.{DatabaseHelpers, DefaultServerSpec, MockApiClient}
import io.bryzek.waivers.api.v0.models as apiModels
import io.bryzek.waivers.api.v0.models.json.*
import io.bryzek.waivers.error.v0.models.json.*
import play.api.libs.json.*
import play.api.test.*
import play.api.test.Helpers.*

class WaiversControllerSpec extends DefaultServerSpec
  with MockApiClient
  with DatabaseHelpers {

  import java.util.UUID

  def uniqueSlug(): String = s"test-${UUID.randomUUID().toString.take(8)}"

  "getProject" must {
    "return 200 with project data when project exists" in {
      val project = createProject(name = "Test Project", slug = uniqueSlug())

      val request = FakeRequest(GET, s"/projects/${project.slug}")
      val response = route(app, request).get

      status(response) mustBe OK
      val result = contentAsJson(response).as[apiModels.Project]
      result.id must startWith("prj-")
      result.id mustBe project.id
      result.name mustBe "Test Project"
      result.slug mustBe project.slug
    }

    "return 404 when project not found" in {
      val request = FakeRequest(GET, "/projects/nonexistent")
      val response = route(app, request).get

      status(response) mustBe NOT_FOUND
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "project_not_found"
      result.message must include("nonexistent")
    }
  }

  "getCurrentWaiver" must {
    "return 200 with waiver data when current waiver exists" in {
      val project = createProject(slug = uniqueSlug())
      val waiver = createWaiver(project.id, title = "Test Waiver", content = "Test content", isCurrent = true)

      val request = FakeRequest(GET, s"/projects/${project.slug}/waiver")
      val response = route(app, request).get

      status(response) mustBe OK
      val result = contentAsJson(response).as[apiModels.Waiver]
      result.id must startWith("wvr-")
      result.id mustBe waiver.id
      result.title mustBe "Test Waiver"
      result.content mustBe "Test content"
      result.status mustBe apiModels.WaiverStatus.Current
    }

    "return 404 when project not found" in {
      val request = FakeRequest(GET, "/projects/nonexistent/waiver")
      val response = route(app, request).get

      status(response) mustBe NOT_FOUND
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "project_not_found"
    }

    "return 404 when no current waiver exists" in {
      val project = createProject(slug = uniqueSlug())
      // Create waiver but mark as not current
      createWaiver(project.id, isCurrent = false)

      val request = FakeRequest(GET, s"/projects/${project.slug}/waiver")
      val response = route(app, request).get

      status(response) mustBe NOT_FOUND
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "waiver_not_found"
    }
  }

  "createSignature" must {
    "return 201 with signature data when valid form submitted" in {
      val project = createProject(slug = uniqueSlug())
      val waiver = createWaiver(project.id, isCurrent = true)

      val formData = Json.obj(
        "firstName" -> "John",
        "lastName" -> "Doe",
        "email" -> "john.doe@example.com",
        "phone" -> "555-123-4567"
      )

      val request = FakeRequest(POST, s"/projects/${project.slug}/signatures")
        .withJsonBody(formData)
        .withHeaders("Content-Type" -> "application/json")

      val response = route(app, request).get

      status(response) mustBe CREATED
      val json = contentAsJson(response)
      (json \ "user" \ "id").asOpt[String] must be(defined)
      (json \ "user" \ "id").as[String] must startWith("usr-")
      (json \ "waiver" \ "id").as[String] must startWith("wvr-")
      (json \ "waiver" \ "id").as[String] mustBe waiver.id
      (json \ "status").as[String] mustBe "pending"
    }

    "return 400 when form validation fails" in {
      val project = createProject(slug = uniqueSlug())
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
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "project_not_found"
    }

    "return 500 when no current waiver exists" in {
      val project = createProject(slug = uniqueSlug())
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
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "signature_creation_failed"
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
      (json \ "id").as[String] must startWith("sig-")
      (json \ "id").as[String] mustBe signature.id
      (json \ "user" \ "id").as[String] must startWith("usr-")
      (json \ "user" \ "id").as[String] mustBe user.id
      (json \ "waiver" \ "id").as[String] must startWith("wvr-")
      (json \ "waiver" \ "id").as[String] mustBe waiver.id
      (json \ "status").as[String] mustBe "pending"
    }

    "return 404 when signature not found" in {
      val request = FakeRequest(GET, "/signatures/nonexistent")
      val response = route(app, request).get

      status(response) mustBe NOT_FOUND
      val result = contentAsJson(response).as[io.bryzek.waivers.error.v0.models.GenericError]
      result.code mustBe "signature_not_found"
    }
  }
}
