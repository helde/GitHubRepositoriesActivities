package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class WSGitHubCommit(committer: String, date: Date) {
  def getFormatedDate: String = {
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd")
    dateFormat.format(date)
  }
}

object WSGitHubCommit {
  implicit val gitHubProjectSummaryReads: Reads[WSGitHubCommit] = (
    (JsPath \ "email").read[String] and
      (JsPath \ "date").read[Date]
    )(WSGitHubCommit.apply _)

  implicit val gitHubProjectSummaryWriters = new Writes[WSGitHubCommit] {
    def writes(gitHubProjectSummary: WSGitHubCommit) = Json.obj(
      "email"        -> gitHubProjectSummary.committer,
      "date"         -> gitHubProjectSummary.date
    )
  }
}