package helpers

import io.bryzek.waivers.api.v0.Client
import play.api.libs.ws.WSClient
import play.api.Application

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait MockApiClient {
  this: Helpers =>

  def port: Int
  def wsClient(implicit app: Application): WSClient = instanceOf[WSClient]

  def expectError(f: => Future[?]): Throwable = {
    Try {
      await(f)
    } match {
      case Success(_) => sys.error("Expected an error but got a success response")
      case Failure(ex) => ex
    }
  }

  def anonymousClient(implicit app: Application): Client = {
    MockApiClientBuilder()
      .withPort(port)
      .build(wsClient)
  }
}

case class MockApiClientBuilder(
  url: String = "http://localhost",
  port: Option[Int] = None,
) {
  def withUrl(url: String): MockApiClientBuilder = this.copy(url = url)
  def withPort(port: Int): MockApiClientBuilder = this.copy(port = Some(port))

  def build(ws: WSClient): Client = {
    new Client(
      ws = ws,
      baseUrl = port match {
        case None => url
        case Some(p) => s"$url:$p"
      }
    )
  }
}