package models.internal

import io.bryzek.waivers.api.v0.models
import io.bryzek.waivers.api.v0.models.json.*
import play.api.libs.json._
import org.joda.time.DateTime
import javax.inject.Inject

// Internal models that correspond to API Builder generated models
// but are used internally by the application

case class Project(
  id: String,
  name: String,
  slug: String,
  status: ProjectStatus
)

object Project {
  def apply(generated: db.generated.Project): Project = {
    Project(
      id = generated.id,
      name = generated.name,
      slug = generated.slug,
      status = ProjectStatus(generated.status)
    )
  }
}

case class Person(
  email: String,
  firstName: String,
  lastName: String,
  phone: Option[String]
)

case class Waiver(
  id: String,
  projectId: String,
  version: Int,
  content: String
)

object Waiver {
  def apply(generated: db.generated.Waiver): Waiver = {
    Waiver(
      id = generated.id,
      projectId = generated.projectId,
      version = generated.version,
      content = generated.content
    )
  }
}

case class SignatureTemplate(
  id: String,
  projectId: String,
  provider: SignatureProvider,
  providerTemplateId: String,
  name: String,
  status: String
)

object SignatureTemplate {
  def apply(generated: db.generated.SignatureTemplate): SignatureTemplate = {
    SignatureTemplate(
      id = generated.id,
      projectId = generated.projectId,
      provider = SignatureProvider.fromString(generated.provider).getOrElse(SignatureProvider.DocuSign),
      providerTemplateId = generated.providerTemplateId,
      name = generated.name,
      status = generated.status
    )
  }

  implicit val format: Format[SignatureTemplate] = Json.format[SignatureTemplate]
}

case class Signature(
  id: String,
  waiverId: String,
  person: Person,
  status: SignatureStatus,
  signedAt: Option[DateTime],
  pdfUrl: Option[String],
)

object Signature {
  def apply(generated: db.generated.Signature): Signature = {
    Signature(
      id = generated.id,
      waiverId = generated.waiverId,
      person = Person(
        firstName = generated.personFirstName,
        lastName = generated.personLastName,
        email = generated.personEmail,
        phone = generated.personPhone,
      ),
      status = SignatureStatus(generated.status),
      signedAt = generated.signedAt,
      pdfUrl = generated.pdfUrl
    )
  }

  implicit val format: Format[Signature] = Json.format[Signature]
}

sealed trait SignatureProvider {
  def name: String
}

object SignatureProvider {
  case object DocuSign extends SignatureProvider { val name = "docusign" }
  case object AdobeSign extends SignatureProvider { val name = "adobe_sign" }

  def fromString(str: String): Option[SignatureProvider] = str.toLowerCase match {
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


object WaiverForm {
  implicit val format: Format[WaiverForm] = Json.format[WaiverForm]
}

object GenericError {
  implicit val format: Format[GenericError] = Json.format[GenericError]
}
