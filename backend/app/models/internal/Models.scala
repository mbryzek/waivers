package models.internal

import play.api.libs.json._
import org.joda.time.DateTime

// Internal models that correspond to API Builder generated models
// but are used internally by the application

case class Project(
  id: String,
  name: String,
  slug: String,
  description: Option[String],
  waiverTemplate: String,
  isActive: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

object Project {
  def apply(generated: db.generated.Project): Project = {
    Project(
      id = generated.id,
      name = generated.name,
      slug = generated.slug,
      description = generated.description,
      waiverTemplate = generated.waiverTemplate,
      isActive = generated.isActive,
      createdAt = generated.createdAt,
      updatedAt = generated.updatedAt,
      updatedByUserId = generated.updatedByUserId
    )
  }
  
  implicit val format: Format[Project] = Json.format[Project]
}

case class User(
  id: String,
  email: String,
  firstName: String,
  lastName: String,
  phone: Option[String],
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

object User {
  def apply(generated: db.generated.User): User = {
    User(
      id = generated.id,
      email = generated.email,
      firstName = generated.firstName,
      lastName = generated.lastName,
      phone = generated.phone,
      createdAt = generated.createdAt,
      updatedAt = generated.updatedAt,
      updatedByUserId = generated.updatedByUserId
    )
  }
  
  implicit val format: Format[User] = Json.format[User]
}

case class Waiver(
  id: String,
  projectId: String,
  version: Int,
  title: String,
  content: String,
  isCurrent: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

object Waiver {
  def apply(generated: db.generated.Waiver): Waiver = {
    Waiver(
      id = generated.id,
      projectId = generated.projectId,
      version = generated.version,
      title = generated.title,
      content = generated.content,
      isCurrent = generated.isCurrent,
      createdAt = generated.createdAt,
      updatedAt = generated.updatedAt,
      updatedByUserId = generated.updatedByUserId
    )
  }
  
  implicit val format: Format[Waiver] = Json.format[Waiver]
}

case class SignatureTemplate(
  id: String,
  projectId: String,
  provider: SignatureProvider,
  providerTemplateId: String,
  name: String,
  isActive: Boolean,
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

case class SignatureRequest(
  id: String,
  signatureTemplateId: String,
  provider: SignatureProvider,
  providerRequestId: String,
  signingUrl: Option[String],
  status: SignatureRequestStatus,
  metadata: Option[String],
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

case class Signature(
  id: String,
  userId: String,
  waiverId: String,
  signatureTemplateId: Option[String],
  signatureRequestId: Option[String],
  status: SignatureStatus,
  signedAt: Option[DateTime],
  pdfUrl: Option[String],
  ipAddress: Option[String],
  createdAt: DateTime,
  updatedAt: DateTime,
  updatedByUserId: String
)

object Signature {
  def apply(generated: db.generated.Signature): Signature = {
    Signature(
      id = generated.id,
      userId = generated.userId,
      waiverId = generated.waiverId,
      signatureTemplateId = generated.signatureTemplateId,
      signatureRequestId = generated.signatureRequestId,
      status = SignatureStatus.fromString(generated.status).getOrElse(SignatureStatus.Pending),
      signedAt = generated.signedAt,
      pdfUrl = generated.pdfUrl,
      ipAddress = generated.ipAddress,
      createdAt = generated.createdAt,
      updatedAt = generated.updatedAt,
      updatedByUserId = generated.updatedByUserId
    )
  }
  
  implicit val format: Format[Signature] = Json.format[Signature]
}

sealed trait SignatureProvider {
  def name: String
}

object SignatureProvider {
  case object HelloSign extends SignatureProvider { val name = "hello_sign" }
  case object DocuSign extends SignatureProvider { val name = "docusign" }
  case object AdobeSign extends SignatureProvider { val name = "adobe_sign" }

  def fromString(str: String): Option[SignatureProvider] = str.toLowerCase match {
    case "hello_sign" => Some(HelloSign)
    case "docusign" => Some(DocuSign)
    case "adobe_sign" => Some(AdobeSign)
    case _ => None
  }

  implicit val format: Format[SignatureProvider] = Format[SignatureProvider](
    Reads { json =>
      json.validate[String].flatMap { str =>
        fromString(str) match {
          case Some(provider) => JsSuccess(provider)
          case None => JsError(s"Invalid signature provider: $str")
        }
      }
    },
    Writes(provider => JsString(provider.name))
  )
}

sealed trait SignatureRequestStatus {
  def name: String
}

object SignatureRequestStatus {
  case object Created extends SignatureRequestStatus { val name = "created" }
  case object Sent extends SignatureRequestStatus { val name = "sent" }
  case object Viewed extends SignatureRequestStatus { val name = "viewed" }
  case object Completed extends SignatureRequestStatus { val name = "completed" }
  case object Cancelled extends SignatureRequestStatus { val name = "cancelled" }
  case object Declined extends SignatureRequestStatus { val name = "declined" }
  case object Error extends SignatureRequestStatus { val name = "error" }

  def fromString(str: String): Option[SignatureRequestStatus] = str.toLowerCase match {
    case "created" => Some(Created)
    case "sent" => Some(Sent)
    case "viewed" => Some(Viewed)
    case "completed" => Some(Completed)
    case "cancelled" => Some(Cancelled)
    case "declined" => Some(Declined)
    case "error" => Some(Error)
    case _ => None
  }

  implicit val format: Format[SignatureRequestStatus] = Format[SignatureRequestStatus](
    Reads { json =>
      json.validate[String].flatMap { str =>
        fromString(str) match {
          case Some(status) => JsSuccess(status)
          case None => JsError(s"Invalid signature request status: $str")
        }
      }
    },
    Writes(status => JsString(status.name))
  )
}

sealed trait SignatureStatus {
  def name: String
}

object SignatureStatus {
  case object Pending extends SignatureStatus { val name = "pending" }
  case object Signed extends SignatureStatus { val name = "signed" }
  case object Expired extends SignatureStatus { val name = "expired" }
  case object Cancelled extends SignatureStatus { val name = "cancelled" }

  def fromString(str: String): Option[SignatureStatus] = str.toLowerCase match {
    case "pending" => Some(Pending)
    case "signed" => Some(Signed)
    case "expired" => Some(Expired)
    case "cancelled" => Some(Cancelled)
    case _ => None
  }

  implicit val format: Format[SignatureStatus] = Format[SignatureStatus](
    Reads { json =>
      json.validate[String].flatMap { str =>
        fromString(str) match {
          case Some(status) => JsSuccess(status)
          case None => JsError(s"Invalid signature status: $str")
        }
      }
    },
    Writes(status => JsString(status.name))
  )
}

case class WaiverForm(
  firstName: String,
  lastName: String,
  email: String,
  phone: Option[String]
)

case class GenericError(
  code: String,
  message: String
)

// Merged object with constructor and JSON formatter

object User {
  implicit val format: Format[User] = Json.format[User]
}

object Waiver {
  implicit val format: Format[Waiver] = Json.format[Waiver]
}

object SignatureTemplate {
  implicit val format: Format[SignatureTemplate] = Json.format[SignatureTemplate]
}

object SignatureRequest {
  implicit val format: Format[SignatureRequest] = Json.format[SignatureRequest]
}

object Signature {
  implicit val format: Format[Signature] = Json.format[Signature]
}

object WaiverForm {
  implicit val format: Format[WaiverForm] = Json.format[WaiverForm]
}

object GenericError {
  implicit val format: Format[GenericError] = Json.format[GenericError]
}