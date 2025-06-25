package services

import play.api.libs.json.{Json, JsValue}
import sttp.client3._
import sttp.client3.playJson._
import sttp.client3.asynchttpclient.future._
import models.internal._
import util.BackendConfig
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.annotation.unused
import java.util.Base64

@Singleton
class PdfCoService @Inject()(
  backendConfig: BackendConfig
)(implicit ec: ExecutionContext) {

  private val backend = AsyncHttpClientFutureBackend()
  
  private val apiUrl = "https://api.pdf.co/v1"
  private val apiKey = backendConfig.pdfCoConfig.apiKey

  def createSignatureRequest(signature: Signature, user: User, waiver: Waiver): Future[String] = {
    try {
        // Create a simple PDF with waiver content that will be signed
        val waiverContent = s"""
          |${waiver.title}
          |
          |${waiver.content}
          |
          |Signer Information:
          |Name: ${user.firstName} ${user.lastName}
          |Email: ${user.email}
          |${user.phone.map(p => s"Phone: $p").getOrElse("")}
          |
          |By signing below, I acknowledge that I have read and agree to the terms above.
          |
          |Signature: _____________________    Date: _____________________
          |
          |${user.firstName} ${user.lastName}
          |""".stripMargin

        // First, create a PDF from the waiver text
        val createPdfRequest = Json.obj(
          "html" -> s"""
            <html>
            <body style="font-family: Arial, sans-serif; margin: 40px; line-height: 1.6;">
              <div style="white-space: pre-line;">${waiverContent.replace("\n", "<br/>")}</div>
            </body>
            </html>
          """,
          "paperSize" -> "Letter",
          "margins" -> "20px",
          "name" -> s"waiver-${signature.id}.pdf"
        )

        val request = basicRequest
          .header("x-api-key", apiKey)
          .contentType("application/json")
          .body(createPdfRequest)
          .post(uri"$apiUrl/pdf/convert/from/html")
          .response(asJson[JsValue])

        backend.send(request).map { response =>
          response.body match {
            case Right(json: JsValue) =>
              val success = (json \ "error").asOpt[Boolean].getOrElse(false)
              if (!success) {
                val pdfUrl = (json \ "url").as[String]
                println(s"[PDF.co API] Created PDF for signature request: $pdfUrl")
                
                // For PDF.co, we'll return a custom signing URL that includes the PDF URL
                // This will be handled by our frontend to show the PDF and collect signature
                val signingUrl = s"${backendConfig.appBaseUrl}/sign/${signature.id}?pdf=${java.net.URLEncoder.encode(pdfUrl, "UTF-8")}"
                signingUrl
              } else {
                val errorMessage = (json \ "message").asOpt[String].getOrElse("Unknown error")
                println(s"[PDF.co API Error] Failed to create PDF: $errorMessage")
                throw new RuntimeException(s"PDF.co API error: $errorMessage")
              }
            case Left(error) =>
              println(s"[PDF.co API Error] Failed to create PDF: $error")
              throw new RuntimeException(s"PDF.co API error: $error")
          }
        }
    } catch {
      case ex: Exception =>
        println(s"[PDF.co Error] Exception during PDF creation: ${ex.getMessage}")
        throw ex
    }
  }

  def addSignatureToPdf(pdfUrl: String, signatureData: String, x: Int = 450, y: Int = 200): Future[String] = {
    try {
        // Add signature to the PDF using PDF.co's edit API
        val requestBody = Json.obj(
          "url" -> pdfUrl,
          "annotations" -> Json.arr(
            Json.obj(
              "text" -> signatureData,
              "x" -> x,
              "y" -> y,
              "size" -> 12,
              "color" -> "#000000"
            )
          )
        )

        val request = basicRequest
          .header("x-api-key", apiKey)
          .contentType("application/json")
          .body(requestBody)
          .post(uri"$apiUrl/pdf/edit/add")
          .response(asJson[JsValue])

        backend.send(request).map { response =>
          response.body match {
            case Right(json: JsValue) =>
              val success = (json \ "error").asOpt[Boolean].getOrElse(false)
              if (!success) {
                val signedPdfUrl = (json \ "url").as[String]
                println(s"[PDF.co API] Added signature to PDF: $signedPdfUrl")
                signedPdfUrl
              } else {
                val errorMessage = (json \ "message").asOpt[String].getOrElse("Unknown error")
                println(s"[PDF.co API Error] Failed to add signature: $errorMessage")
                throw new RuntimeException(s"PDF.co API error: $errorMessage")
              }
            case Left(error) =>
              println(s"[PDF.co API Error] Failed to add signature: $error")
              throw new RuntimeException(s"PDF.co API error: $error")
          }
        }
    } catch {
      case ex: Exception =>
        println(s"[PDF.co Error] Exception during signature addition: ${ex.getMessage}")
        throw ex
    }
  }

}