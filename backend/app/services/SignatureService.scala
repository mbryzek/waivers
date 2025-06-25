package services

import db.generated.{SignaturesDao, UsersDao, SignatureTemplatesDao, SignatureRequestsDao}
import db.generated.{Signature => GeneratedSignature, User => GeneratedUser, SignatureForm => GeneratedSignatureForm}
import models.internal._
import cats.data.ValidatedNec
import cats.implicits._
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
  pdfCoService: PdfCoService
)(implicit ec: ExecutionContext) {

  def createSignature(project: Project, form: WaiverForm, ipAddress: String): Future[ValidatedNec[String, (Signature, User, Waiver)]] = {
    for {
      // Find or create user
      user <- findOrCreateUser(form)

      // Get current waiver for project
      waiverOpt <- waiverService.findCurrentByProjectId(project.id)
      waiver = waiverOpt.getOrElse(
        throw new RuntimeException(s"No current waiver found for project ${project.id}")
      )

      // Create signature record
      signature <- createSignatureRecord(user, waiver, ipAddress)

      // Create PDF.co signature request
      signatureRequestResult <- pdfCoService.createSignatureRequest(signature, user, waiver)

      // Handle the ValidatedNec result
      result <- signatureRequestResult match {
        case cats.data.Validated.Valid(signatureRequestId) =>
          // Update signature with PDF.co request ID
          updateSignatureWithRequestId(signature.id, signatureRequestId).map { _ =>
            (signature, user, waiver).validNec[String]
          }
        case cats.data.Validated.Invalid(errors) =>
          Future.successful(errors.invalid)
      }

    } yield result
  }

  def findById(id: String): Future[Option[Signature]] = Future {
    signaturesDao.findById(id).map(Signature(_))
  }

  def findByIdWithRelated(id: String): Future[Option[(Signature, User, Waiver)]] = {
    signaturesDao.findById(id) match {
      case None => Future.successful(None)
      case Some(generatedSignature) =>
        val signature = Signature(generatedSignature)
        for {
          userOpt <- Future(usersDao.findById(signature.userId).map(User(_)))
          waiverOpt <- waiverService.findById(signature.waiverId)
        } yield {
          for {
            user <- userOpt
            waiver <- waiverOpt
          } yield (signature, user, waiver)
        }
    }
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
        val updatedForm = generatedUser.form.copy(
          email = form.email,
          lowerEmail = form.email.toLowerCase,
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone,
          updatedAt = DateTime.now(),
          updatedByUserId = "system" // TODO: Get proper user context
        )
        usersDao.updateById("system", generatedUser.id, updatedForm)
        User(usersDao.findById(generatedUser.id).getOrElse(generatedUser))

      case None =>
        val userForm = db.generated.UserForm(
          email = form.email,
          lowerEmail = form.email.toLowerCase,
          firstName = form.firstName,
          lastName = form.lastName,
          phone = form.phone,
          createdAt = DateTime.now(),
          updatedAt = DateTime.now(),
          updatedByUserId = "system", // TODO: Get proper user context
          hashCode = System.currentTimeMillis()
        )
        val insertedUser = usersDao.insert("system", userForm)
        User(insertedUser)
    }
  }

  private def createSignatureRecord(user: User, waiver: Waiver, ipAddress: String): Future[Signature] = Future {
    val signatureForm = GeneratedSignatureForm(
      userId = user.id,
      waiverId = waiver.id,
      signatureTemplateId = None, // Will be set when signature template is selected
      signatureRequestId = None, // Will be set when signature request is created
      status = "pending",
      signedAt = None,
      pdfUrl = None,
      ipAddress = Some(ipAddress),
      createdAt = DateTime.now(),
      updatedAt = DateTime.now(),
      updatedByUserId = "system", // TODO: Get proper user context
      hashCode = System.currentTimeMillis()
    )

    val insertedSignature = signaturesDao.insert("system", signatureForm)
    Signature(insertedSignature)
  }

  private def updateSignatureWithRequestId(signatureId: String, requestId: String): Future[Unit] = Future {
    signaturesDao.findById(signatureId).foreach { existingSignature =>
      val updatedForm = existingSignature.form.copy(
        signatureRequestId = Some(requestId),
        updatedAt = DateTime.now(),
        updatedByUserId = "system" // TODO: Get proper user context
      )
      signaturesDao.updateById("system", signatureId, updatedForm)
    }
  }

  def markSignatureAsSigned(signatureId: String, signatureData: String): Future[Option[Signature]] = Future {
    signaturesDao.findById(signatureId).map { existingSignature =>
      val updatedForm = existingSignature.form.copy(
        status = "signed", // Using string instead of SignatureStatus.Signed.name
        signedAt = Some(DateTime.now()),
        updatedAt = DateTime.now(),
        updatedByUserId = "system" // TODO: Get proper user context
      )
      signaturesDao.updateById("system", signatureId, updatedForm)
      Signature(signaturesDao.findById(signatureId).getOrElse(
        throw new RuntimeException(s"Failed to find updated signature with id $signatureId")
      ))
    }
  }
}
