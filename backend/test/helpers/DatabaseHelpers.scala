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
    
    val projectForm = ProjectForm(
      name = name,
      slug = slug,
      description = description,
      waiverTemplate = waiverTemplate,
      status = if (isActive) "active" else "inactive"
    )
    
    val insertedProjectId = projectsDao.insert("test-user", projectForm)
    Project(projectsDao.findById(insertedProjectId).getOrElse(
      throw new RuntimeException(s"Failed to create project with id $insertedProjectId")
    ))
  }

  def createUser(
    email: String = randomEmail(),
    firstName: String = randomString(),
    lastName: String = randomString(),
    phone: Option[String] = None
  )(implicit app: Application): User = {
    val usersDao = instanceOf[UsersDao]
    
    val userForm = UserForm(
      email = email,
      lowerEmail = email.toLowerCase,
      firstName = firstName,
      lastName = lastName,
      phone = phone
    )
    
    val insertedUserId = usersDao.insert("test-user", userForm)
    User(usersDao.findById(insertedUserId).getOrElse(
      throw new RuntimeException(s"Failed to create user with id $insertedUserId")
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
    
    val waiverForm = WaiverForm(
      projectId = projectId,
      version = version,
      title = title,
      content = content,
      status = if (isCurrent) "current" else "archived"
    )
    
    val insertedWaiverId = waiversDao.insert("test-user", waiverForm)
    Waiver(waiversDao.findById(insertedWaiverId).getOrElse(
      throw new RuntimeException(s"Failed to create waiver with id $insertedWaiverId")
    ))
  }

  def createSignature(
    userId: String,
    waiverId: String,
    status: SignatureStatus = SignatureStatus.Pending,
    ipAddress: Option[String] = Some("127.0.0.1")
  )(implicit app: Application): Signature = {
    val signaturesDao = instanceOf[SignaturesDao]
    
    val signatureForm = SignatureForm(
      userId = userId,
      waiverId = waiverId,
      signatureTemplateId = None,
      signatureRequestId = None,
      status = status.name,
      signedAt = None,
      pdfUrl = None,
      ipAddress = ipAddress
    )
    
    val insertedSignatureId = signaturesDao.insert("test-user", signatureForm)
    Signature(signaturesDao.findById(insertedSignatureId).getOrElse(
      throw new RuntimeException(s"Failed to create signature with id $insertedSignatureId")
    ))
  }
}