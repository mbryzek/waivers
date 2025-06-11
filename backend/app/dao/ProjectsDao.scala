package dao

import anorm._
import anorm.SqlParser._
import models.internal.Project
import play.api.db.Database
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class ProjectsDao @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val parser: RowParser[Project] = {
    get[String]("id") ~
    get[String]("name") ~
    get[String]("slug") ~
    get[Option[String]]("description") ~
    get[String]("waiver_template") ~
    get[Option[String]]("hellosign_template_id") ~
    get[Boolean]("is_active") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") ~
    get[String]("updated_by_user_id") map {
      case id ~ name ~ slug ~ description ~ waiverTemplate ~ helloSignTemplateId ~ isActive ~ createdAt ~ updatedAt ~ updatedByUserId =>
        Project(id, name, slug, description, waiverTemplate, helloSignTemplateId, isActive, createdAt, updatedAt, updatedByUserId)
    }
  }

  def findBySlug(slug: String): Future[Option[Project]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM projects WHERE slug = $slug AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def findById(id: String): Future[Option[Project]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM projects WHERE id = $id AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def findAll(limit: Long, offset: Long): Future[Seq[Project]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM projects WHERE deleted_at IS NULL ORDER BY created_at DESC LIMIT $limit OFFSET $offset"
        .as(parser.*)
    }
  }

  def insert(project: Project): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO projects (
          id, name, slug, description, waiver_template, hellosign_template_id, is_active,
          created_at, updated_at, updated_by_user_id
        ) VALUES (
          ${project.id}, ${project.name}, ${project.slug}, ${project.description}, 
          ${project.waiverTemplate}, ${project.helloSignTemplateId}, ${project.isActive},
          ${project.createdAt}, ${project.updatedAt}, ${project.updatedByUserId}
        )
      """.executeUpdate()
    }
  }

  def update(project: Project): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE projects SET
          name = ${project.name},
          slug = ${project.slug},
          description = ${project.description},
          waiver_template = ${project.waiverTemplate},
          hellosign_template_id = ${project.helloSignTemplateId},
          is_active = ${project.isActive},
          updated_at = ${project.updatedAt},
          updated_by_user_id = ${project.updatedByUserId}
        WHERE id = ${project.id}
      """.executeUpdate()
    }
  }
}