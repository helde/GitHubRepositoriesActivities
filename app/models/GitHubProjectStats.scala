package models

case class CommitProjection(date: String, nbCommits: Int)

case class GitHubProjectStats(fullName: String,
                              totalCommits: Int,
                              commitProjection: Seq[CommitProjection],
                              committers: Set[CommitterStats],
                              contributors: Set[WSGitHubContributor])
