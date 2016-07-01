package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class WSGitHubProjectSummary(name: String,
                                  fullName: String,
                                  description: Option[String],
                                  updated: Date,
                                  url: String) {
  def getFormatedDate: String = {
    val dateFormat: SimpleDateFormat = new SimpleDateFormat("dd MMM yyyy")
    dateFormat.format(updated)
  }
}

object WSGitHubProjectSummary {
  implicit val gitHubProjectSummaryReads: Reads[WSGitHubProjectSummary] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "full_name").read[String] and
      (JsPath \ "description").readNullable[String] and
      (JsPath \ "updated_at").read[Date] and
      (JsPath \ "html_url").read[String]
    )(WSGitHubProjectSummary.apply _)

  implicit val gitHubProjectSummaryWriters = new Writes[WSGitHubProjectSummary] {
    def writes(gitHubProjectSummary: WSGitHubProjectSummary) = Json.obj(
      "name"        -> JsString(gitHubProjectSummary.name),
      "full_name"    -> JsString(gitHubProjectSummary.fullName),
      "description" -> Json.toJson(gitHubProjectSummary.description),
      "updated_at"     -> gitHubProjectSummary.updated,
      "url"         -> JsString(gitHubProjectSummary.url)
    )
  }
}

case class WSGitHubProjectsSummary(totalCount: Int,
                                   projects: Seq[WSGitHubProjectSummary])

object WSGitHubProjectsSummary {
  implicit val gitHubProjectsSummaryReads: Reads[WSGitHubProjectsSummary] = (
    (JsPath \ "total_count").read[Int] and
      (JsPath \ "items").read[Seq[WSGitHubProjectSummary]]
    )(WSGitHubProjectsSummary.apply _)

  implicit val gitHubProjectsSummaryWriters = new Writes[WSGitHubProjectsSummary] {
    def writes(gitHubProjectsSummary: WSGitHubProjectsSummary) = Json.obj(
      "total_count" -> JsNumber(gitHubProjectsSummary.totalCount),
      "items"       -> gitHubProjectsSummary.projects
    )
  }
}