package filters

import org.apache.pekko.stream.Materializer
import play.api.http.HttpFilters
import play.api.mvc.*
import play.filters.cors.CORSFilter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
  * Custom filter that enables CORS for cross-origin requests from the frontend
  */
class CorsWithLoggingFilter @javax.inject.Inject() (corsFilter: CORSFilter, loggingFilter: LoggingFilter) extends HttpFilters {
  def filters: Seq[EssentialFilter] = Seq(corsFilter, loggingFilter)
}

class LoggingFilter @Inject() (
) (
  implicit val mat: Materializer, ec: ExecutionContext
) extends Filter {

  def apply(nextFilter: RequestHeader => Future[Result])
           (requestHeader: RequestHeader): Future[Result] = {

    val startTime = System.currentTimeMillis()
    
    nextFilter(requestHeader).map { result =>
      val requestTime = System.currentTimeMillis() - startTime
      
      val line = s"${requestHeader.method} ${requestHeader.path} ${result.header.status} ${requestTime}ms"
      println(line) // Simple logging to console
      
      result
    }
  }
}