package services

import play.api.libs.ws.WSClient
import javax.inject.Inject

import models._
import play.api.libs.json.{JsValue, Json}
import play.mvc.Http

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class GithubApiService @Inject() (ws: WSClient) {
  val githubApiUrl = "https://api.github.com"

  private def extractGithubCommitInformations(body: JsValue): Option[WSGitHubCommit] = {
    val commitOpt = (body \ "commit").asOpt[JsValue]
    commitOpt.flatMap { commit =>
      val committerJson = (commit \ "committer").as[JsValue]
      val parsingResult = Json.parse(committerJson.toString()).validate[WSGitHubCommit]
      parsingResult.fold(
        parsingFailure => None,
        (gitHubCommit: WSGitHubCommit) => Some(gitHubCommit)
      )
    }
  }

  private def getCommitStatsForProject(projectFullName: String): Future[Seq[WSGitHubCommit]] = {
    ws.url(s"$githubApiUrl/repos/$projectFullName/commits?per_page=100").get().map { response =>
      response.status match {
        case Http.Status.OK =>
          val commitsJson = response.json.as[Seq[JsValue]]
          commitsJson.flatMap(extractGithubCommitInformations)

        case _ =>
          println(response.status)
          Seq.empty
      }
    }
  }

  private def getContributorsForProject(projectFullName: String): Future[Set[WSGitHubContributor]] = {
    ws.url(s"$githubApiUrl/repos/$projectFullName/stats/contributors").get().flatMap { response =>
      response.status match {
        case Http.Status.OK =>
          val parsingResult = Json.parse(response.body).validate[Set[WSGitHubContributor]]
          parsingResult.fold(
            parsingFailure => Future.failed(new RuntimeException(parsingFailure.toString())),
            (gitHubContributor: Set[WSGitHubContributor]) => Future.successful(gitHubContributor)
          )

        case _ => Future.successful(Set.empty)
      }
    }
  }
  def getRepositoriesAtPage(projectName: String, currentPage: Int): Future[WSGitHubProjectsSummary] = {
    val futureResponse = ws.url(s"$githubApiUrl/search/repositories?q=$projectName&per_page=10&page=$currentPage").get()

    futureResponse.flatMap { response =>
      response.status match {
        case Http.Status.OK => {
          val parsingResult = Json.parse(response.body).validate[WSGitHubProjectsSummary]
          parsingResult.fold(
            parsingFailure => Future.failed(new RuntimeException(parsingFailure.toString())),
            (gitHubProjectSummary: WSGitHubProjectsSummary) => Future.successful(gitHubProjectSummary)
          )
        }
        case _ => Future.failed(new RuntimeException(s"Unexpected response status ${response.status}"))
      }
    }
  }


  def getRepositoryStats(projectFullName: String): Future[Option[GitHubProjectStats]] = {
    getCommitStatsForProject(projectFullName).flatMap { commits =>
      val dates:Seq[String] = commits.map(_.getFormatedDate).distinct.sorted
      val commitProjection: Seq[CommitProjection] = dates.map { date =>
        CommitProjection(date, commits.count(_.getFormatedDate == date))
      }

      val committerEmails: Set[String] = commits.map(_.committer).toSet

      val committers = committerEmails.map { email =>
        CommitterStats(email, commits.count(_.committer == email))
      }

      getContributorsForProject(projectFullName).map { contributors =>
        val totalCommits = contributors.foldLeft(0)((a, b) => a + b.totalCommits)

        if (committers == Set.empty) {
          None
        } else {
          Some(GitHubProjectStats(projectFullName, totalCommits, commitProjection, committers, contributors))
        }
      }
    }
  }
}
