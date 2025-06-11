package controllers

import play.api.mvc._
import play.api.libs.json._
import services.HelloSignService
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WebhookController @Inject()(
  cc: ControllerComponents,
  helloSignService: HelloSignService
)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def helloSignWebhook(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    helloSignService.handleWebhook(request.body) map { _ =>
      Ok(Json.obj("status" -> "processed"))
    } recover {
      case ex =>
        // Log the error but return 200 to prevent webhook retries
        play.api.Logger("webhook").error(s"Error processing HelloSign webhook: ${ex.getMessage}", ex)
        Ok(Json.obj("status" -> "error", "message" -> ex.getMessage))
    }
  }
}