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
    projectsDao.findAll(limit = Some(1))(using query => query.equals("projects.slug", Some(slug))).headOption.map(toInternal)
  }

  def findById(id: String): Future[Option[Project]] = Future {
    projectsDao.findById(id).map(toInternal)
  }

  def findAll(limit: Long = 50, offset: Long = 0): Future[Seq[Project]] = Future {
    projectsDao.findAll(limit = Some(limit), offset = offset).map(toInternal)
  }

  def create(name: String, slug: String, description: Option[String], waiverTemplate: String, status: String = "active"): Future[Project] = Future {
    val projectForm = GeneratedProjectForm(
      name = name,
      slug = slug,
      description = description,
      waiverTemplate = waiverTemplate,
      status = status
    )
    
    // Insert the project
    val insertedProjectId = projectsDao.insert(projectForm)
    val insertedProject = projectsDao.findById(insertedProjectId).get
    
    // Create a default waiver for the project
    val waiverForm = GeneratedWaiverForm(
      projectId = insertedProject.id,
      version = 1,
      title = s"$name Waiver",
      content = waiverTemplate,
      status = "current"
    )
    
    waiversDao.insert(waiverForm)
    
    // Return the created project
    toInternal(insertedProject)
  }

  def update(id: String, name: String, slug: String, description: Option[String], waiverTemplate: String, status: String): Future[Option[Project]] = {
    Future {
      projectsDao.findById(id).map { existingProject =>
        val updatedForm = existingProject.form.copy(
          name = name,
          slug = slug,
          description = description,
          waiverTemplate = waiverTemplate,
          status = status
        )
        
        projectsDao.updateById(id, updatedForm)
        
        // Return the updated project
        projectsDao.findById(id).map(toInternal).getOrElse(toInternal(existingProject))
      }
    }
  }
}