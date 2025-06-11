package services

import dao.{SignaturesDao, UsersDao}
import models.internal._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import java.time.Instant
import scala.annotation.unused

@Singleton
class SignatureService @Inject()(
  signaturesDao: SignaturesDao,
  usersDao: UsersDao,
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

  def findById(id: String): Future[Option[Signature]] = {
    signaturesDao.findById(id)
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

  private def findOrCreateUser(form: WaiverForm): Future[User] = {
    usersDao.findByEmail(form.email).flatMap {
      case Some(user) => 
        // Update user with latest information
        val updatedUser = user.copy(
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone,
          updatedAt = Instant.now(),
          updatedByUserId = "system" // TODO: Get proper user context
        )
        usersDao.update(updatedUser).map(_ => updatedUser)
      
      case None =>
        val newUser = User(
          id = s"usr-${UUID.randomUUID().toString.replace("-", "")}",
          email = form.email,
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone,
          createdAt = Instant.now(),
          updatedAt = Instant.now(),
          updatedByUserId = "system" // TODO: Get proper user context
        )
        usersDao.insert(newUser).map(_ => newUser)
    }
  }

  private def createSignatureRecord(user: User, waiver: Waiver, ipAddress: String): Future[Signature] = {
    val signature = Signature(
      id = s"sig-${UUID.randomUUID().toString.replace("-", "")}",
      userId = user.id,
      waiverId = waiver.id,
      helloSignSignatureRequestId = None,
      status = SignatureStatus.Pending,
      signedAt = None,
      pdfUrl = None,
      ipAddress = Some(ipAddress),
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      updatedByUserId = "system" // TODO: Get proper user context
    )
    
    signaturesDao.insert(signature).map(_ => signature)
  }
}