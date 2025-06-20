package services

import db.generated.{ProjectsDao, WaiversDao, Project => GeneratedProject, ProjectForm => GeneratedProjectForm, WaiverForm => GeneratedWaiverForm}
import models.internal.Project
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import org.joda.time.DateTime

@Singleton
class ProjectService @Inject()(
  projectsDao: ProjectsDao,
  waiversDao: WaiversDao
)(implicit ec: ExecutionContext) {

  // Conversion methods between internal and generated models
  private def toInternal(generated: GeneratedProject): Project = Project(generated)

  def findBySlug(slug: String): Future[Option[Project]] = Future {
    projectsDao.findAll(limit = Some(1))(query => query.equals("projects.slug", Some(slug))).headOption.map(toInternal)
  }

  def findById(id: String): Future[Option[Project]] = Future {
    projectsDao.findById(id).map(toInternal)
  }

  def findAll(limit: Long = 50, offset: Long = 0): Future[Seq[Project]] = Future {
    projectsDao.findAll(limit = Some(limit), offset = offset).map(toInternal)
  }

  def create(name: String, slug: String, description: Option[String], waiverTemplate: String, isActive: Boolean = true): Future[Project] = Future {
    val projectId = s"prj-${UUID.randomUUID().toString.replace("-", "")}"
    val projectForm = GeneratedProjectForm(
      id = projectId,
      name = name,
      slug = slug,
      description = description,
      waiverTemplate = waiverTemplate,
      isActive = isActive
    )
    
    // Insert the project
    projectsDao.insert("system", projectForm) // TODO: Get proper user context
    
    // Create a default waiver for the project
    val waiverId = s"wvr-${UUID.randomUUID().toString.replace("-", "")}"
    val waiverForm = GeneratedWaiverForm(
      id = waiverId,
      projectId = projectId,
      version = 1,
      title = s"$name Waiver",
      content = waiverTemplate,
      isCurrent = true
    )
    
    waiversDao.insert("system", waiverForm) // TODO: Get proper user context
    
    // Return the created project
    projectsDao.findById(projectId).map(toInternal).getOrElse(
      throw new RuntimeException(s"Failed to create project with id $projectId")
    )
  }

  def update(id: String, name: String, slug: String, description: Option[String], waiverTemplate: String, isActive: Boolean): Future[Option[Project]] = {
    findById(id).map { maybeProject =>
      maybeProject.map { existingProject =>
        val form = GeneratedProjectForm(
          id = id,
          name = name,
          slug = slug,
          description = description,
          waiverTemplate = waiverTemplate,
          isActive = isActive
        )
        
        projectsDao.updateById("system", id, form) // TODO: Get proper user context
        
        // Return the updated project
        projectsDao.findById(id).map(toInternal).getOrElse(existingProject)
      }
    }
  }
}