package app.domain

import java.util.UUID

object job {
  case class Job(id: UUID,
                 postDate: Long,
                 ownerEmail: String,
                 jobInfo: JobInfo,
                 active: Boolean)

  case class JobInfo(company: String,
                     image: Option[String] = None,
                     title: String,
                     desc: String,
                     remote: Boolean,
                     externalUrl: String,
                     salaryHigh: Option[Int] = None,
                     salaryLow: Option[Int] = None,
                     currency: Option[String] = None,
                     location: Option[String] = None,
                     country: Option[String] = None,
                     tags:  Option[List[String]] = None,
                     seniority: Option[String] = None,
                     other: Option[String] = None)

  object Job {
    val jobInfo: JobInfo = JobInfo.empty
    val empty: Job =
      Job(UUID.randomUUID(), 0, "", jobInfo, false)
  }

  object JobInfo {
    val empty: JobInfo =
      JobInfo("", None, "", "", false, "", None, None, None, None, None, None, None, None)
  }

  case class JobFilter(company: List[String] = List(),
                       remote: Boolean = false,
                       location: List[String] = List(),
                       country: List[String] = List(),
                       tags: List[String] = List(),
                       seniority: List[String] = List(),
                       salaryLow: Option[Int] = None,
                      )
}


