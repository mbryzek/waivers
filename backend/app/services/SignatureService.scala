package services

import db.generated.{SignaturesDao, UsersDao, SignatureTemplatesDao, SignatureRequestsDao}
import db.generated.{Signature => GeneratedSignature, User => GeneratedUser, SignatureForm => GeneratedSignatureForm}
import models.internal._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import org.joda.time.DateTime
import scala.annotation.unused

@Singleton
class SignatureService @Inject()(
  signaturesDao: SignaturesDao,
  usersDao: UsersDao,
  @unused signatureTemplatesDao: SignatureTemplatesDao,
  @unused signatureRequestsDao: SignatureRequestsDao,
  waiverService: WaiverService,
  @unused helloSignService: HelloSignService
)(implicit ec: ExecutionContext) {

  def createSignature(project: Project, form: WaiverForm, ipAddress: String): Future[Signature] = {
    for {
      // Find or create user
      user <- findOrCreateUser(form)
      
      // Get current waiver for project
      waiver <- waiverService.findCurrentByProjectId(project.id).map(_.getOrElse(
        throw new RuntimeException(s"No current waiver found for project ${project.id}")
      ))
      
      // Create signature record
      signature <- createSignatureRecord(user, waiver, ipAddress)
      
      // Initiate HelloSign process (placeholder for now)
      _ <- Future.successful(()) // TODO: Integrate with HelloSign
      
    } yield signature
  }

  def findById(id: String): Future[Option[Signature]] = Future {
    signaturesDao.findById(id).map(Signature(_))
  }

  def findWithFilters(
    projectId: Option[String], 
    status: Option[String], 
    email: Option[String], 
    limit: Long, 
    offset: Long
  ): Future[Seq[Signature]] = {
    // TODO: Implement filtering logic
    // For now, return empty list as placeholder
    Future.successful(Seq.empty)
  }

  private def findOrCreateUser(form: WaiverForm): Future[User] = Future {
    // Try to find user by email using the query builder
    usersDao.findAll(limit = Some(1))(query => query.equals("users.email", Some(form.email))).headOption match {
      case Some(generatedUser) =>
        // Update user with latest information
        val userForm = db.generated.UserForm(
          id = generatedUser.id,
          email = form.email,
          lowerEmail = form.email.toLowerCase,
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone
        )
        usersDao.updateById("system", generatedUser.id, userForm) // TODO: Get proper user context
        User(usersDao.findById(generatedUser.id).getOrElse(generatedUser))
        
      case None =>
        val userId = s"usr-${UUID.randomUUID().toString.replace("-", "")}"
        val userForm = db.generated.UserForm(
          id = userId,
          email = form.email,
          lowerEmail = form.email.toLowerCase,
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone
        )
        usersDao.insert("system", userForm) // TODO: Get proper user context
        User(usersDao.findById(userId).getOrElse(
          throw new RuntimeException(s"Failed to create user with id $userId")
        ))
    }
  }

  private def createSignatureRecord(user: User, waiver: Waiver, ipAddress: String): Future[Signature] = Future {
    val signatureId = s"sig-${UUID.randomUUID().toString.replace("-", "")}"
    val signatureForm = GeneratedSignatureForm(
      id = signatureId,
      userId = user.id,
      waiverId = waiver.id,
      signatureTemplateId = None, // Will be set when signature template is selected
      signatureRequestId = None, // Will be set when signature request is created
      status = SignatureStatus.Pending.name,
      signedAt = None,
      pdfUrl = None,
      ipAddress = Some(ipAddress)
    )
    
    signaturesDao.insert("system", signatureForm) // TODO: Get proper user context
    Signature(signaturesDao.findById(signatureId).getOrElse(
      throw new RuntimeException(s"Failed to create signature with id $signatureId")
    ))
  }
}