package controllers

import play.api.mvc._
import play.api.libs.json._
import javax.inject._

@Singleton
class HealthcheckController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def index(): Action[AnyContent] = Action { implicit request =>
    Ok(Json.obj(
      "status" -> "healthy",
      "service" -> "waivers",
      "version" -> "0.0.1"
    ))
  }
}