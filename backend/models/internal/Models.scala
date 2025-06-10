package models.internal

import play.api.libs.json._
import java.time.Instant

// Internal models that correspond to API Builder generated models
// but are used internally by the application

case class Project(
  id: String,
  name: String,
  slug: String,
  description: Option[String],
  waiverTemplate: String,
  signNowTemplateId: Option[String],
  isActive: Boolean,
  createdAt: Instant,
  updatedAt: Instant,
  updatedByUserId: String
)

case class User(
  id: String,
  email: String,
  firstName: String,
  lastName: String,
  phone: Option[String],
  createdAt: Instant,
  updatedAt: Instant,
  updatedByUserId: String
)

case class Waiver(
  id: String,
  projectId: String,
  version: Int,
  title: String,
  content: String,
  isCurrent: Boolean,
  createdAt: Instant,
  updatedAt: Instant,
  updatedByUserId: String
)

case class Signature(
  id: String,
  userId: String,
  waiverId: String,
  signNowDocumentId: Option[String],
  status: SignatureStatus,
  signedAt: Option[Instant],
  pdfUrl: Option[String],
  ipAddress: Option[String],
  createdAt: Instant,
  updatedAt: Instant,
  updatedByUserId: String
)

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

// JSON formatters
object Project {
  implicit val format: Format[Project] = Json.format[Project]
}

object User {
  implicit val format: Format[User] = Json.format[User]
}

object Waiver {
  implicit val format: Format[Waiver] = Json.format[Waiver]
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