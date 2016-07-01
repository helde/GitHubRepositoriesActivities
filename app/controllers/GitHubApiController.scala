package controllers

import javax.inject.Inject
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import services.GithubApiService

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GitHubApiController @Inject() (githubApiService: GithubApiService) extends Controller {

  def searchGithubProject = Action {
    Ok(views.html.searchGithubProject())
  }

  private val searchForm = Form {
    single(
      "projectName" -> nonEmptyText
    )
  }

  def searchRepositories = Action.async { implicit request =>
    searchForm.bindFromRequest.fold(
      error => Future(Redirect(routes.GitHubApiController.searchGithubProject())),
      success = {
        case projectName =>
          Future(Redirect(routes.GitHubApiController.searchRepositoriesAtPage(projectName, 1)))
      }
    )
  }

  def searchRepositoriesAtPage(projectName: String, currentPage: Int) = Action.async { implicit request =>
    if (currentPage <= 0) {
      Future(Redirect(routes.GitHubApiController.searchGithubProject()))
    } else {
      githubApiService.getRepositoriesAtPage(projectName, currentPage).map { gitHubProjects =>
        Ok(views.html.selectProject(projectName, gitHubProjects, currentPage, 10))
      }
    }
  }

  def projectAnAlytics(projectFullName: String) = Action.async { implicit request =>
    githubApiService.getRepositoryStats(projectFullName).map { gitHubProject =>
      Ok(views.html.projectData(gitHubProject))
    }
  }

}
