package helpers

import org.scalatestplus.play._
import play.api.Application
import play.api.test.Helpers._
import java.util.UUID
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

trait Helpers extends PlaySpec {

  def instanceOf[T](implicit app: Application, manifest: Manifest[T]): T = {
    app.injector.instanceOf[T]
  }

  def await[T](future: Future[T]): T = {
    Await.result(future, 30.seconds)
  }

  def randomString(length: Int = 10): String = {
    UUID.randomUUID().toString.replace("-", "").take(length)
  }

  def randomEmail(): String = {
    s"test-${randomString()}@test.waivers.com"
  }

  def randomSlug(): String = {
    s"test-${randomString()}"
  }
}