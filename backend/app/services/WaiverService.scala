package services

import db.generated.{WaiversDao, Waiver => GeneratedWaiver}
import models.internal.Waiver
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import org.joda.time.DateTime

@Singleton
class WaiverService @Inject()(
  waiversDao: WaiversDao
)(implicit ec: ExecutionContext) {

  private def toInternal(generated: GeneratedWaiver): Waiver = Waiver(generated)

  def findCurrentByProjectId(projectId: String): Future[Option[Waiver]] = Future {
    waiversDao.findAll(limit = Some(1))(customQueryModifier = query =>
      query
        .equals("waivers.project_id", Some(projectId))
        .equals("waivers.is_current", Some(true))
    ).headOption.map(toInternal)
  }

  def findById(id: String): Future[Option[Waiver]] = Future {
    waiversDao.findById(id).map(toInternal)
  }
}