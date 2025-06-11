package services

import dao.ProjectsDao
import models.internal.Project
import controllers.ProjectForm
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import java.time.Instant

@Singleton
class ProjectService @Inject()(
  projectsDao: ProjectsDao
)(implicit ec: ExecutionContext) {

  def findBySlug(slug: String): Future[Option[Project]] = {
    projectsDao.findBySlug(slug)
  }

  def findById(id: String): Future[Option[Project]] = {
    projectsDao.findById(id)
  }

  def findAll(limit: Long = 50, offset: Long = 0): Future[Seq[Project]] = {
    projectsDao.findAll(limit, offset)
  }

  def create(form: ProjectForm): Future[Project] = {
    val project = Project(
      id = s"prj-${UUID.randomUUID().toString.replace("-", "")}",
      name = form.name,
      slug = form.slug,
      description = form.description,
      waiverTemplate = form.waiverTemplate,
      helloSignTemplateId = None,
      isActive = form.isActive,
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
      updatedByUserId = "system" // TODO: Get proper user context
    )
    
    projectsDao.insert(project).map(_ => project)
  }

  def update(id: String, form: ProjectForm): Future[Option[Project]] = {
    findById(id).flatMap {
      case Some(existingProject) =>
        val updatedProject = existingProject.copy(
          name = form.name,
          slug = form.slug,
          description = form.description,
          waiverTemplate = form.waiverTemplate,
          isActive = form.isActive,
          updatedAt = Instant.now(),
          updatedByUserId = "system" // TODO: Get proper user context
        )
        projectsDao.update(updatedProject).map(_ => Some(updatedProject))
      
      case None =>
        Future.successful(None)
    }
  }
}