package helpers

import db.generated.{ProjectsDao, UsersDao, WaiversDao, SignaturesDao}
import db.generated.{ProjectForm, UserForm, WaiverForm, SignatureForm}
import models.internal.{Project, User, Waiver, Signature, SignatureStatus}
import java.util.UUID
import org.joda.time.DateTime
import play.api.Application

trait DatabaseHelpers {
  this: Helpers =>

  def createProject(
    name: String = randomString(),
    slug: String = randomSlug(),
    description: Option[String] = Some(randomString()),
    waiverTemplate: String = s"Test waiver template for ${randomString()}",
    isActive: Boolean = true
  )(implicit app: Application): Project = {
    val projectsDao = instanceOf[ProjectsDao]
    val projectId = s"prj-${UUID.randomUUID().toString.replace("-", "")}"
    
    val projectForm = ProjectForm(
      id = projectId,
      name = name,
      slug = slug,
      description = description,
      waiverTemplate = waiverTemplate,
      isActive = isActive
    )
    
    projectsDao.insert("test-user", projectForm)
    Project(projectsDao.findById(projectId).getOrElse(
      throw new RuntimeException(s"Failed to create project with id $projectId")
    ))
  }

  def createUser(
    email: String = randomEmail(),
    firstName: String = randomString(),
    lastName: String = randomString(),
    phone: Option[String] = None
  )(implicit app: Application): User = {
    val usersDao = instanceOf[UsersDao]
    val userId = s"usr-${UUID.randomUUID().toString.replace("-", "")}"
    
    val userForm = UserForm(
      id = userId,
      email = email,
      lowerEmail = email.toLowerCase,
      firstName = firstName,
      lastName = lastName,
      phone = phone
    )
    
    usersDao.insert("test-user", userForm)
    User(usersDao.findById(userId).getOrElse(
      throw new RuntimeException(s"Failed to create user with id $userId")
    ))
  }

  def createWaiver(
    projectId: String,
    version: Int = 1,
    title: String = s"Test Waiver ${randomString()}",
    content: String = s"Test waiver content ${randomString()}",
    isCurrent: Boolean = true
  )(implicit app: Application): Waiver = {
    val waiversDao = instanceOf[WaiversDao]
    val waiverId = s"wvr-${UUID.randomUUID().toString.replace("-", "")}"
    
    val waiverForm = WaiverForm(
      id = waiverId,
      projectId = projectId,
      version = version,
      title = title,
      content = content,
      isCurrent = isCurrent
    )
    
    waiversDao.insert("test-user", waiverForm)
    Waiver(waiversDao.findById(waiverId).getOrElse(
      throw new RuntimeException(s"Failed to create waiver with id $waiverId")
    ))
  }

  def createSignature(
    userId: String,
    waiverId: String,
    status: SignatureStatus = SignatureStatus.Pending,
    ipAddress: Option[String] = Some("127.0.0.1")
  )(implicit app: Application): Signature = {
    val signaturesDao = instanceOf[SignaturesDao]
    val signatureId = s"sig-${UUID.randomUUID().toString.replace("-", "")}"
    
    val signatureForm = SignatureForm(
      id = signatureId,
      userId = userId,
      waiverId = waiverId,
      signatureTemplateId = None,
      signatureRequestId = None,
      status = status.name,
      signedAt = None,
      pdfUrl = None,
      ipAddress = ipAddress
    )
    
    signaturesDao.insert("test-user", signatureForm)
    Signature(signaturesDao.findById(signatureId).getOrElse(
      throw new RuntimeException(s"Failed to create signature with id $signatureId")
    ))
  }
}