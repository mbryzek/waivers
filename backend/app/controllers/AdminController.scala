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
class AdminController @Inject()(
  cc: ControllerComponents,
  projectService: ProjectService,
  signatureService: SignatureService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def getProjects(limit: Option[Long], offset: Option[Long]): Action[AnyContent] = Action.async { implicit request =>
    val actualLimit = limit.getOrElse(50L)
    val actualOffset = offset.getOrElse(0L)
    
    projectService.findAll(actualLimit, actualOffset) map { projects =>
      Ok(Json.toJson(projects))
    }
  }

  def createProject(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val result = for {
      form <- request.body.validate[ProjectForm].asEither.leftMap(errors => 
        List(GenericError("validation_failed", "Invalid form data: " + JsError.toJson(errors)))
      )
      validatedForm <- validateProjectForm(form)
    } yield validatedForm

    result match {
      case Left(errors) => Future.successful(BadRequest(Json.toJson(errors)))
      case Right(form) =>
        projectService.create(form.name, form.slug, form.description, form.waiverTemplate, if (form.isActive) "active" else "inactive") map { project =>
          Created(Json.toJson(project))
        } recover {
          case ex => InternalServerError(Json.obj("code" -> "project_creation_failed", "message" -> ex.getMessage))
        }
    }
  }

  def getProject(id: String): Action[AnyContent] = Action.async { implicit request =>
    projectService.findById(id) map {
      case Some(project) => Ok(Json.toJson(project))
      case None => NotFound(Json.obj("code" -> "project_not_found", "message" -> s"Project with id '$id' not found"))
    }
  }

  def updateProject(id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val result = for {
      form <- request.body.validate[ProjectForm].asEither.leftMap(errors => 
        List(GenericError("validation_failed", "Invalid form data: " + JsError.toJson(errors)))
      )
      validatedForm <- validateProjectForm(form)
    } yield validatedForm

    result match {
      case Left(errors) => Future.successful(BadRequest(Json.toJson(errors)))
      case Right(form) =>
        projectService.update(id, form.name, form.slug, form.description, form.waiverTemplate, if (form.isActive) "active" else "inactive") map {
          case Some(project) => Ok(Json.toJson(project))
          case None => NotFound(Json.obj("code" -> "project_not_found", "message" -> s"Project with id '$id' not found"))
        } recover {
          case ex => InternalServerError(Json.obj("code" -> "project_update_failed", "message" -> ex.getMessage))
        }
    }
  }

  def getSignatures(
    projectId: Option[String], 
    status: Option[String], 
    email: Option[String], 
    limit: Option[Long], 
    offset: Option[Long]
  ): Action[AnyContent] = Action.async { implicit request =>
    val actualLimit = limit.getOrElse(50L)
    val actualOffset = offset.getOrElse(0L)
    
    signatureService.findWithFilters(projectId, status, email, actualLimit, actualOffset) map { signatures =>
      Ok(Json.toJson(signatures))
    }
  }

  def exportSignatures(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val result = for {
      exportRequest <- request.body.validate[ExportRequest].asEither.leftMap(errors => 
        List(GenericError("validation_failed", "Invalid export request: " + JsError.toJson(errors)))
      )
    } yield exportRequest

    result match {
      case Left(errors) => Future.successful(BadRequest(Json.toJson(errors)))
      case Right(exportRequest) =>
        // TODO: Implement export functionality
        Future.successful(NotImplemented(Json.obj("code" -> "not_implemented", "message" -> "Export functionality not yet implemented")))
    }
  }

  private def validateProjectForm(form: ProjectForm): Either[List[GenericError], ProjectForm] = {
    val validations: ValidatedNec[String, ProjectForm] = (
      validateNonEmpty("name", form.name),
      validateSlug("slug", form.slug),
      validateNonEmpty("waiver_template", form.waiverTemplate)
    ).mapN { (_, _, _) => form }

    validations.toEither.leftMap(_.toList.map(GenericError("validation_failed", _)))
  }

  private def validateNonEmpty(field: String, value: String): ValidatedNec[String, String] = {
    if (value.trim.nonEmpty) value.valid
    else s"$field cannot be empty".invalidNec
  }

  private def validateSlug(field: String, value: String): ValidatedNec[String, String] = {
    val slugRegex = """^[a-z0-9-]+$""".r
    if (slugRegex.matches(value)) value.valid
    else s"$field must contain only lowercase letters, numbers, and hyphens".invalidNec
  }
}

// Additional models for admin functionality
case class ProjectForm(
  name: String,
  slug: String,
  description: Option[String],
  waiverTemplate: String,
  isActive: Boolean = true
)

case class ExportRequest(
  projectId: String,
  status: Option[String],
  fromDate: Option[String],
  toDate: Option[String]
)

object ProjectForm {
  implicit val format: Format[ProjectForm] = Json.format[ProjectForm]
}

object ExportRequest {
  implicit val format: Format[ExportRequest] = Json.format[ExportRequest]
}