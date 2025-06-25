package util

import play.api.Configuration
import javax.inject.Inject

case class PdfCoConfig(apiKey: String)

class BackendConfig @Inject() (
  cfg: PlayConfigWrapper
) {

  private def getHost(name: String): String = {
    val h = cfg.requiredString(name)
    assert(h.startsWith("http"), s"Backend host '$h' must start with http")
    assert(!h.endsWith("/"), s"Backend host '$h' must not end with /")
    h
  }

  val name: String = "Waivers Backend"
  val appBaseUrl: String = getHost("app.base.url")

  val pdfCoConfig: PdfCoConfig = PdfCoConfig(
    apiKey = cfg.requiredString("pdfco.api.key")
  )

  def isHealthy: Boolean = true
}

class PlayConfigWrapper @Inject() (playConfig: Configuration) {
  def requiredString(name: String): String = {
    optionalString(name).getOrElse {
      sys.error(s"Missing environment variable '$name'")
    }
  }

  def requiredInt(name: String): Int = {
    val value = requiredString(name)
    value.toIntOption.getOrElse {
      sys.error(s"Invalid value '$value' for environment variable '$name': Must be an integer.")
    }
  }

  private def optionalString(name: String): Option[String] = {
    playConfig.getOptional[String](name).map(_.trim).filter(_.nonEmpty)
  }

  def optionalBoolean(name: String): Option[Boolean] = {
    playConfig.getOptional[String](name).map(_.trim).filter(_.nonEmpty) match {
      case None => None
      case Some(v) => {
        v.toLowerCase().trim.take(1) match {
          case "t" => Some(true)
          case "f" => Some(false)
        }
      }
    }
  }
}