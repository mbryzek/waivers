package controllers

import play.api.mvc._
import play.api.libs.json._
import services._
import models.internal._
import cats.data.ValidatedNec
import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}
import javax.inject._

@Singleton
class WaiversController @Inject()(
  cc: ControllerComponents,
  projectService: ProjectService,
  waiverService: WaiverService,
  signatureService: SignatureService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getProject(slug: String): Action[AnyContent] = Action.async { implicit request =>
    projectService.findBySlug(slug) map {
      case Some(project) => Ok(Json.toJson(project))
      case None => NotFound(Json.obj("code" -> "project_not_found", "message" -> s"Project with slug '$slug' not found"))
    }
  }

  def getCurrentWaiver(slug: String): Action[AnyContent] = Action.async { implicit request =>
    for {
      projectOpt <- projectService.findBySlug(slug)
      result <- projectOpt match {
        case None => Future.successful(NotFound(Json.obj("code" -> "project_not_found", "message" -> s"Project with slug '$slug' not found")))
        case Some(project) =>
          waiverService.findCurrentByProjectId(project.id) map {
            case Some(waiver) => Ok(Json.toJson(waiver))
            case None => NotFound(Json.obj("code" -> "waiver_not_found", "message" -> "No current waiver found for this project"))
          }
      }
    } yield result
  }

  def createSignature(slug: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val result = for {
      form <- request.body.validate[WaiverForm].asEither.leftMap(errors => 
        List(GenericError("validation_failed", "Invalid form data: " + JsError.toJson(errors)))
      )
      validatedForm <- validateWaiverForm(form)
    } yield validatedForm

    result match {
      case Left(errors) => Future.successful(BadRequest(Json.toJson(errors)))
      case Right(form) =>
        for {
          projectOpt <- projectService.findBySlug(slug)
          result <- projectOpt match {
            case None => Future.successful(NotFound(Json.obj("code" -> "project_not_found", "message" -> s"Project with slug '$slug' not found")))
            case Some(project) =>
              signatureService.createSignature(project, form, request.remoteAddress) map { signature =>
                Created(Json.toJson(signature))
              } recover {
                case ex => InternalServerError(Json.obj("code" -> "signature_creation_failed", "message" -> ex.getMessage))
              }
          }
        } yield result
    }
  }

  def getSignature(id: String): Action[AnyContent] = Action.async { implicit request =>
    signatureService.findById(id) map {
      case Some(signature) => Ok(Json.toJson(signature))
      case None => NotFound(Json.obj("code" -> "signature_not_found", "message" -> s"Signature with id '$id' not found"))
    }
  }

  private def validateWaiverForm(form: WaiverForm): Either[List[GenericError], WaiverForm] = {
    val validations: ValidatedNec[String, WaiverForm] = (
      validateNonEmpty("first_name", form.firstName),
      validateNonEmpty("last_name", form.lastName),
      validateEmail("email", form.email),
      validatePhone("phone", form.phone)
    ).mapN { (_, _, _, _) => form }

    validations.toEither.leftMap(_.toList.map(GenericError("validation_failed", _)))
  }

  private def validateNonEmpty(field: String, value: String): ValidatedNec[String, String] = {
    if (value.trim.nonEmpty) value.valid
    else s"$field cannot be empty".invalidNec
  }

  private def validateEmail(field: String, value: String): ValidatedNec[String, String] = {
    val emailRegex = """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".r
    if (emailRegex.matches(value)) value.valid
    else s"$field must be a valid email address".invalidNec
  }

  private def validatePhone(field: String, value: Option[String]): ValidatedNec[String, Option[String]] = {
    value match {
      case None | Some("") => None.valid
      case Some(phone) =>
        val phoneRegex = """^[\+]?[\d\s\-\(\)]{10,}$""".r
        if (phoneRegex.matches(phone)) Some(phone).valid
        else s"$field must be a valid phone number".invalidNec
    }
  }
}