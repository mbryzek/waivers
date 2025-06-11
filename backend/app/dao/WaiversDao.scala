package dao

import anorm._
import anorm.SqlParser._
import models.internal.Waiver
import play.api.db.Database
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class WaiversDao @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val parser: RowParser[Waiver] = {
    get[String]("id") ~
    get[String]("project_id") ~
    get[Int]("version") ~
    get[String]("title") ~
    get[String]("content") ~
    get[Boolean]("is_current") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") ~
    get[String]("updated_by_user_id") map {
      case id ~ projectId ~ version ~ title ~ content ~ isCurrent ~ createdAt ~ updatedAt ~ updatedByUserId =>
        Waiver(id, projectId, version, title, content, isCurrent, createdAt, updatedAt, updatedByUserId)
    }
  }

  def findCurrentByProjectId(projectId: String): Future[Option[Waiver]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM waivers WHERE project_id = $projectId AND is_current = true AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def findById(id: String): Future[Option[Waiver]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM waivers WHERE id = $id AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def insert(waiver: Waiver): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO waivers (
          id, project_id, version, title, content, is_current,
          created_at, updated_at, updated_by_user_id
        ) VALUES (
          ${waiver.id}, ${waiver.projectId}, ${waiver.version}, ${waiver.title}, 
          ${waiver.content}, ${waiver.isCurrent},
          ${waiver.createdAt}, ${waiver.updatedAt}, ${waiver.updatedByUserId}
        )
      """.executeUpdate()
    }
  }
}