package services

import helpers.{DefaultAppSpec, DatabaseHelpers}
import models.internal._
import scala.concurrent.ExecutionContext.Implicits.global

class SignatureServiceSpec extends DefaultAppSpec with DatabaseHelpers {

  private def service: SignatureService = instanceOf[SignatureService]

  "createSignature" must {
    "create a new signature with new user" in {
      val project = createProject()
      val waiver = createWaiver(project.id)
      
      val form = WaiverForm(
        firstName = "John",
        lastName = "Doe",
        email = randomEmail(),
        phone = Some("555-1234")
      )
      
      val (signature, user, createdWaiver) = await(service.createSignature(project, form, "127.0.0.1"))
      
      signature.status.mustBe(SignatureStatus.Pending)
      signature.ipAddress.mustBe(Some("127.0.0.1"))
      signature.waiverId.mustBe(waiver.id)
      signature.signedAt.mustBe(None)
      signature.pdfUrl.mustBe(None)
      user.email.mustBe(form.email)
      user.firstName.mustBe(form.firstName)
      user.lastName.mustBe(form.lastName)
      createdWaiver.id.mustBe(waiver.id)
    }

    "create a new signature with existing user" in {
      val project = createProject()
      val waiver = createWaiver(project.id)
      val email = randomEmail()
      
      // Create user first
      val existingUser = createUser(email = email, firstName = "Jane", lastName = "Smith")
      
      val form = WaiverForm(
        firstName = "Jane Updated",
        lastName = "Smith Updated",
        email = email,
        phone = Some("555-5678")
      )
      
      val (signature, user, createdWaiver) = await(service.createSignature(project, form, "192.168.1.1"))
      
      signature.status.mustBe(SignatureStatus.Pending)
      signature.ipAddress.mustBe(Some("192.168.1.1"))
      signature.waiverId.mustBe(waiver.id)
      signature.userId.mustBe(existingUser.id)
      user.id.mustBe(existingUser.id)
      createdWaiver.id.mustBe(waiver.id)
    }

    "fail when no current waiver exists for project" in {
      val project = createProject()
      // Create waiver but mark it as not current
      createWaiver(project.id, isCurrent = false)
      
      val form = WaiverForm(
        firstName = "John",
        lastName = "Doe",
        email = randomEmail(),
        phone = None
      )
      
      intercept[RuntimeException] {
        await(service.createSignature(project, form, "127.0.0.1"))
      }.getMessage must include("No current waiver found")
    }
  }

  "findById" must {
    "return signature when found" in {
      val project = createProject()
      val waiver = createWaiver(project.id)
      val user = createUser()
      val signature = createSignature(user.id, waiver.id)
      
      val result = await(service.findById(signature.id))
      
      result.must(be(defined))
      result.get.id.mustBe(signature.id)
      result.get.userId.mustBe(user.id)
      result.get.waiverId.mustBe(waiver.id)
    }

    "return None when not found" in {
      val result = await(service.findById(randomString()))
      result.mustBe(None)
    }
  }

  "findWithFilters" must {
    "return empty list for now (placeholder implementation)" in {
      val result = await(service.findWithFilters(
        projectId = Some(randomString()),
        status = Some("pending"),
        email = Some(randomEmail()),
        limit = 10,
        offset = 0
      ))
      
      result.mustBe(empty)
    }
  }
}