package services

import dao.WaiversDao
import models.internal.Waiver
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import scala.annotation.unused

@Singleton
class WaiverService @Inject()(
  waiversDao: WaiversDao
)(implicit @unused ec: ExecutionContext) {

  def findCurrentByProjectId(projectId: String): Future[Option[Waiver]] = {
    waiversDao.findCurrentByProjectId(projectId)
  }

  def findById(id: String): Future[Option[Waiver]] = {
    waiversDao.findById(id)
  }
}