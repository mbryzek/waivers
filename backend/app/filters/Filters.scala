package filters

import akka.stream.Materializer
import play.api.http.HttpFilters
import play.api.mvc.*
import play.filters.cors.CORSFilter
import util.{WaiverLogger, ResponseHeaders}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
  * See https://github.com/flowvault/proxy/blob/main/app/filters/Filters.scala
  */
class CorsWithLoggingFilter @javax.inject.Inject() (corsFilter: CORSFilter, loggingFilter: LoggingFilter) extends HttpFilters {
  def filters: Seq[EssentialFilter] = Seq(corsFilter, loggingFilter)
}

class LoggingFilter @Inject() (
  logger: WaiverLogger
) (
  implicit val mat: Materializer, ec: ExecutionContext
) extends Filter {

  private val LoggedHeaders = Seq(
    "User-Agent",
    "X-Forwarded-For",
    "CF-Connecting-IP",
    "True-Client-IP",
  ).map(_.toLowerCase)

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val headerMap = requestHeader.headers.toMap

    def get(name: String): String = headerMap.getOrElse(name, Nil).mkString(",")

    TimedFuture(nextFilter(requestHeader)).map { timedResult =>
      val result = timedResult.value
      val requestTime = timedResult.durationMs

      val line = Seq(
        s"v${requestHeader.version}",
        requestHeader.method,
        s"${requestHeader.host}${requestHeader.path}",
        result.header.status.toString,
        s"${requestTime}ms",
        get("User-Agent"),
        get("X-Forwarded-For"),
        get("CF-Connecting-IP"),
      ).map(_.trim).filterNot(_.isEmpty).mkString(" ")

      logger
        .withKeyValue("https", requestHeader.secure)
        .withKeyValue("http_version", requestHeader.version)
        .withKeyValue("method", requestHeader.method)
        .withKeyValue("host", requestHeader.host)
        .withKeyValue("path", requestHeader.path)
        .withKeyValue("query_params", toString(requestHeader.queryString))
        .withKeyValue("http_code", result.header.status)
        .withKeyValue("request_time_ms", requestTime)
        .withKeyValue("request_headers", toString(headerMap.view.filterKeys(LoggedHeaders.contains).toMap))
        .info(line)

      val requestTimeStr = requestTime.toString
      result
        .withHeaders(ResponseHeaders.RequestTime -> requestTimeStr)
        .withHeaders(ResponseHeaders.ResponseTime -> requestTimeStr)

    }
  }

  private def toString(params: Map[String, Seq[String]]): String = {
    params.flatMap { case (k, values) =>
      values.map { v => s"$k=$v" }
    }.mkString("&")
  }
}
