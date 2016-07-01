package models

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WSGitHubAuthor(login: String, avatarUrl: String)

object WSGitHubAuthor {
  implicit val gitHubAuthorReads: Reads[WSGitHubAuthor] = (
    (JsPath \ "login").read[String] and
      (JsPath \ "avatar_url").read[String]
    )(WSGitHubAuthor.apply _)

  implicit val gitHubAuthorWriters = new Writes[WSGitHubAuthor] {
    def writes(gitHubAuthor: WSGitHubAuthor) = Json.obj(
      "login"        -> gitHubAuthor.login,
      "avatar_url"    -> gitHubAuthor.avatarUrl
    )
  }
}

case class WSGitHubContributor(totalCommits: Int, author: WSGitHubAuthor)

object WSGitHubContributor {
  implicit val gitHubContributorReads: Reads[WSGitHubContributor] = (
    (JsPath \ "total").read[Int] and
      (JsPath \ "author").read[WSGitHubAuthor]
    )(WSGitHubContributor.apply _)

  implicit val gitHubContributorWriters = new Writes[WSGitHubContributor] {
    def writes(gitHubContributor: WSGitHubContributor) = Json.obj(
      "totalCommits"        -> gitHubContributor.totalCommits,
      "author"              -> gitHubContributor.author
    )
  }
}