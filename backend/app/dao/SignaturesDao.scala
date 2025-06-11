package dao

import anorm._
import anorm.SqlParser._
import models.internal.{Signature, SignatureStatus}
import play.api.db.Database
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.time.Instant

@Singleton
class SignaturesDao @Inject()(db: Database)(implicit ec: ExecutionContext) {

  private val parser: RowParser[Signature] = {
    get[String]("id") ~
    get[String]("user_id") ~
    get[String]("waiver_id") ~
    get[Option[String]]("hellosign_signature_request_id") ~
    get[String]("status") ~
    get[Option[Instant]]("signed_at") ~
    get[Option[String]]("pdf_url") ~
    get[Option[String]]("ip_address") ~
    get[Instant]("created_at") ~
    get[Instant]("updated_at") ~
    get[String]("updated_by_user_id") map {
      case id ~ userId ~ waiverId ~ helloSignSignatureRequestId ~ statusStr ~ signedAt ~ pdfUrl ~ ipAddress ~ createdAt ~ updatedAt ~ updatedByUserId =>
        val status = SignatureStatus.fromString(statusStr).getOrElse(SignatureStatus.Pending)
        Signature(id, userId, waiverId, helloSignSignatureRequestId, status, signedAt, pdfUrl, ipAddress, createdAt, updatedAt, updatedByUserId)
    }
  }

  def findById(id: String): Future[Option[Signature]] = Future {
    db.withConnection { implicit connection =>
      SQL"SELECT * FROM signatures WHERE id = $id AND deleted_at IS NULL"
        .as(parser.singleOpt)
    }
  }

  def findByProjectId(projectId: String, limit: Long, offset: Long): Future[Seq[Signature]] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        SELECT s.* FROM signatures s
        INNER JOIN waivers w ON s.waiver_id = w.id
        WHERE w.project_id = $projectId AND s.deleted_at IS NULL
        ORDER BY s.created_at DESC
        LIMIT $limit OFFSET $offset
      """.as(parser.*)
    }
  }

  def insert(signature: Signature): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        INSERT INTO signatures (
          id, user_id, waiver_id, hellosign_signature_request_id, status, signed_at, pdf_url, ip_address,
          created_at, updated_at, updated_by_user_id
        ) VALUES (
          ${signature.id}, ${signature.userId}, ${signature.waiverId}, ${signature.helloSignSignatureRequestId}, 
          ${signature.status.name}, ${signature.signedAt}, ${signature.pdfUrl}, ${signature.ipAddress},
          ${signature.createdAt}, ${signature.updatedAt}, ${signature.updatedByUserId}
        )
      """.executeUpdate()
    }
  }

  def update(signature: Signature): Future[Unit] = Future {
    db.withConnection { implicit connection =>
      SQL"""
        UPDATE signatures SET
          hellosign_signature_request_id = ${signature.helloSignSignatureRequestId},
          status = ${signature.status.name},
          signed_at = ${signature.signedAt},
          pdf_url = ${signature.pdfUrl},
          updated_at = ${signature.updatedAt},
          updated_by_user_id = ${signature.updatedByUserId}
        WHERE id = ${signature.id}
      """.executeUpdate()
    }
  }
}