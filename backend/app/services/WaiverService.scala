package services

import com.mbryzek.util.OrderBy
import db.generated.{WaiversDao, Waiver as GeneratedWaiver}
import io.bryzek.waivers.api.v0.models.WaiverStatus
import models.internal.Waiver

import javax.inject.*
import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime

@Singleton
class WaiverService @Inject()(
  waiversDao: WaiversDao
)(implicit ec: ExecutionContext) {

  private def toInternal(generated: GeneratedWaiver): Waiver = Waiver(generated)

  def findCurrentByProjectId(projectId: String): Future[Option[Waiver]] = Future {
    waiversDao.findAll(limit = Some(1), orderBy = Some(OrderBy("-created_at")))(using query =>
      query
        .equals("waivers.project_id", projectId)
        .equals("waivers.status", WaiverStatus.Current.toString)
    ).headOption.map(toInternal)
  }

  def findById(id: String): Future[Option[Waiver]] = Future {
    waiversDao.findById(id).map(toInternal)
  }
}
