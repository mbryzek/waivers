package services

import db.generated.{ProjectsDao, Project, ProjectForm}
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}
import java.util.UUID
import org.joda.time.DateTime

@Singleton
class ProjectService @Inject()(
  projectsDao: ProjectsDao
)(implicit ec: ExecutionContext) {

  def findBySlug(slug: String): Future[Option[Project]] = Future {
    projectsDao.findAll(limit = Some(1))(query => query.equals("projects.slug", Some(slug))).headOption
  }

  def findById(id: String): Future[Option[Project]] = Future {
    projectsDao.findById(id)
  }

  def findAll(limit: Long = 50, offset: Long = 0): Future[Seq[Project]] = Future {
    projectsDao.findAll(limit = Some(limit), offset = offset)
  }

  def create(name: String, slug: String, description: Option[String], waiverTemplate: String, isActive: Boolean = true): Future[Project] = Future {
    val projectId = s"prj-${UUID.randomUUID().toString.replace("-", "")}"
    val form = ProjectForm(
      id = projectId,
      name = name,
      slug = slug,
      description = description,
      waiverTemplate = waiverTemplate,
      isActive = isActive
    )
    
    projectsDao.insert("system", form) // TODO: Get proper user context
    
    // Return the created project
    projectsDao.findById(projectId).getOrElse(
      throw new RuntimeException(s"Failed to create project with id $projectId")
    )
  }

  def update(id: String, name: String, slug: String, description: Option[String], waiverTemplate: String, isActive: Boolean): Future[Option[Project]] = {
    findById(id).map { maybeProject =>
      maybeProject.map { existingProject =>
        val form = ProjectForm(
          id = id,
          name = name,
          slug = slug,
          description = description,
          waiverTemplate = waiverTemplate,
          isActive = isActive
        )
        
        projectsDao.updateById("system", id, form) // TODO: Get proper user context
        
        // Return the updated project
        projectsDao.findById(id).getOrElse(existingProject)
      }
    }
  }
}