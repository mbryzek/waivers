package services

import helpers.{DefaultAppSpec, DatabaseHelpers}
import scala.concurrent.ExecutionContext.Implicits.global

class WaiverServiceSpec extends DefaultAppSpec with DatabaseHelpers {

  private def service: WaiverService = instanceOf[WaiverService]

  "findCurrentByProjectId" must {
    "return current waiver when found" in {
      val project = createProject()
      val currentWaiver = createWaiver(project.id, version = 2, isCurrent = true)
      // Create older version that's not current
      createWaiver(project.id, version = 1, isCurrent = false)
      
      val result = await(service.findCurrentByProjectId(project.id))
      
      result must be(defined)
      result.get.id mustBe currentWaiver.id
      result.get.projectId mustBe project.id
      result.get.version mustBe 2
      result.get.status mustBe "current"
    }

    "return None when no current waiver exists" in {
      val project = createProject()
      // Create waiver but mark it as not current
      createWaiver(project.id, isCurrent = false)
      
      val result = await(service.findCurrentByProjectId(project.id))
      
      result mustBe None
    }

    "return None when project has no waivers" in {
      val project = createProject()
      
      val result = await(service.findCurrentByProjectId(project.id))
      
      result mustBe None
    }

    "return only current waiver when multiple versions exist" in {
      val project = createProject()
      
      // Create multiple versions
      createWaiver(project.id, version = 1, title = "Version 1", isCurrent = false)
      createWaiver(project.id, version = 2, title = "Version 2", isCurrent = false)
      val currentWaiver = createWaiver(project.id, version = 3, title = "Version 3", isCurrent = true)
      
      val result = await(service.findCurrentByProjectId(project.id))
      
      result must be(defined)
      result.get.id mustBe currentWaiver.id
      result.get.version mustBe 3
      result.get.title mustBe "Version 3"
      result.get.status mustBe "current"
    }
  }

  "findById" must {
    "return waiver when found" in {
      val project = createProject()
      val waiver = createWaiver(project.id, title = "Test Waiver", content = "Test content")
      
      val result = await(service.findById(waiver.id))
      
      result must be(defined)
      result.get.id mustBe waiver.id
      result.get.projectId mustBe project.id
      result.get.title mustBe "Test Waiver"
      result.get.content mustBe "Test content"
    }

    "return None when not found" in {
      val result = await(service.findById(randomString()))
      result mustBe None
    }
  }
}