package dao

import anorm._
import anorm.SqlParser._
import models.internal.User
import play.api.db.Database
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class UsersDao @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val parser: RowParser[User] = {
    get[String]("id") ~
    get[String]("email") ~
    get[String]("first_name") ~
    get[String]("last_name") ~
    get[Option[String]]("phone") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") ~
    get[String]("updated_by_user_id") map {
      case id ~ email ~ firstName ~ lastName ~ phone ~ createdAt ~ updatedAt ~ updatedByUserId =>
        User(id, email, firstName, lastName, phone, createdAt, updatedAt, updatedByUserId)
    }
  }

  def findByEmail(email: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM users WHERE email = $email AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def findById(id: String): Future[Option[User]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM users WHERE id = $id AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def insert(user: User): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO users (
          id, email, first_name, last_name, phone,
          created_at, updated_at, updated_by_user_id
        ) VALUES (
          ${user.id}, ${user.email}, ${user.firstName}, ${user.lastName}, ${user.phone},
          ${user.createdAt}, ${user.updatedAt}, ${user.updatedByUserId}
        )
      """.executeUpdate()
    }
  }

  def update(user: User): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE users SET
          email = ${user.email},
          first_name = ${user.firstName},
          last_name = ${user.lastName},
          phone = ${user.phone},
          updated_at = ${user.updatedAt},
          updated_by_user_id = ${user.updatedByUserId}
        WHERE id = ${user.id}
      """.executeUpdate()
    }
  }
}