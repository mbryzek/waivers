package services

import play.api.Configuration
import play.api.libs.json._
import sttp.client3._
import sttp.client3.playJson._
import sttp.client3.asynchttpclient.future._
import models.internal._
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.annotation.unused
import java.util.Base64

@Singleton
class HelloSignService @Inject()(
  config: Configuration
)(implicit ec: ExecutionContext) {

  private val backend = AsyncHttpClientFutureBackend()
  
  private val apiUrl = config.get[String]("hellosign.api.url")
  private val apiKey = config.getOptional[String]("hellosign.api.key")
  private val clientId = config.getOptional[String]("hellosign.client.id")

  def createSignatureRequest(signature: Signature, user: User, waiver: Waiver): Future[String] = {
    apiKey match {
      case Some(key) =>
        // Real HelloSign API integration
        val requestBody = Json.obj(
          "test_mode" -> true, // Set to false for production
          "title" -> waiver.title,
          "subject" -> s"Please sign: ${waiver.title}",
          "message" -> "Please review and sign the attached waiver document.",
          "signers" -> Json.arr(
            Json.obj(
              "email_address" -> user.email,
              "name" -> s"${user.firstName} ${user.lastName}",
              "order" -> 0
            )
          ),
          "file_url" -> Json.arr("https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf"), // TODO: Use real waiver PDF
          "signing_redirect_url" -> s"${config.getOptional[String]("app.base.url").getOrElse("http://localhost:9300")}/demo/sign-complete"
        )

        val request = basicRequest
          .auth.basic(key, "")
          .contentType("application/json")
          .body(requestBody)
          .post(uri"$apiUrl/signature_request")
          .response(asJson[JsValue])

        backend.send(request).map { response =>
          response.body match {
            case Right(json) =>
              val signatureRequestId = (json \ "signature_request" \ "signature_request_id").as[String]
              println(s"[HelloSign API] Created signature request $signatureRequestId for user ${user.email}")
              signatureRequestId
            case Left(error) =>
              println(s"[HelloSign API Error] Failed to create signature request: $error")
              throw new RuntimeException(s"HelloSign API error: $error")
          }
        }
      case None =>
        // Fallback for demo when no API key is configured
        val mockRequestId = s"hellosign-request-${signature.id}"
        println(s"[HelloSign Demo] No API key configured, using mock request ID: $mockRequestId")
        Future.successful(mockRequestId)
    }
  }

  def getSigningUrl(signatureRequestId: String, signerEmail: String): Future[Option[String]] = {
    apiKey match {
      case Some(key) =>
        // Real HelloSign API call to get embedded signing URL
        val request = basicRequest
          .auth.basic(key, "")
          .get(uri"$apiUrl/signature_request/$signatureRequestId")
          .response(asJson[JsValue])

        backend.send(request).flatMap { response =>
          response.body match {
            case Right(json) =>
              // Find the signature ID for this signer
              val signatures = (json \ "signature_request" \ "signatures").as[JsArray]
              signatures.value.find { sig =>
                (sig \ "signer_email_address").as[String] == signerEmail
              } match {
                case Some(signatureJs) =>
                  val signatureId = (signatureJs \ "signature_id").as[String]
                  
                  // Get embedded signing URL
                  val embeddedRequest = basicRequest
                    .auth.basic(key, "")
                    .post(uri"$apiUrl/embedded/sign_url/$signatureId")
                    .response(asJson[JsValue])

                  backend.send(embeddedRequest).map { embeddedResponse =>
                    embeddedResponse.body match {
                      case Right(embeddedJson) =>
                        val signUrl = (embeddedJson \ "embedded" \ "sign_url").as[String]
                        println(s"[HelloSign API] Generated embedded signing URL for $signerEmail")
                        Some(signUrl)
                      case Left(error) =>
                        println(s"[HelloSign API Error] Failed to get embedded signing URL: $error")
                        None
                    }
                  }
                case None =>
                  println(s"[HelloSign API Error] No signature found for email $signerEmail in request $signatureRequestId")
                  Future.successful(None)
              }
            case Left(error) =>
              println(s"[HelloSign API Error] Failed to get signature request: $error")
              Future.successful(None)
          }
        }
      case None =>
        // Fallback for demo when no API key is configured
        val mockSigningUrl = s"https://app.hellosign.com/sign/embedded?signature_id=demo_${signatureRequestId.takeRight(8)}&token=demo_token_${System.currentTimeMillis()}"
        println(s"[HelloSign Demo] No API key configured, using mock URL: $mockSigningUrl")
        Future.successful(Some(mockSigningUrl))
    }
  }

  def getSignedDocumentPdf(signatureRequestId: String): Future[Option[Array[Byte]]] = {
    // Placeholder implementation
    // In a real implementation, this would:
    // 1. Authenticate with HelloSign API
    // 2. Download the completed PDF document
    // 3. Return the PDF bytes
    
    Future.successful(None)
  }

  def handleWebhook(payload: JsValue): Future[Unit] = {
    // Placeholder implementation
    // In a real implementation, this would:
    // 1. Validate webhook signature using event hash
    // 2. Parse webhook payload for event type and signature request
    // 3. Update signature status in database based on event
    // 4. Send email notification to user if completed
    
    Future.successful(())
  }

  def cancelSignatureRequest(signatureRequestId: String): Future[Unit] = {
    // Placeholder implementation
    // In a real implementation, this would:
    // 1. Authenticate with HelloSign API
    // 2. Cancel the signature request
    // 3. Update status in database
    
    Future.successful(())
  }
}