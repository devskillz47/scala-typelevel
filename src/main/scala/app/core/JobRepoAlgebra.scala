package app.core

import app.domain.job.{Job, JobFilter, JobInfo}
import app.domain.pagination.Pagination

import java.util.UUID

trait JobRepoAlgebra[F[_]] {
  def getAllJobs: F[List[Job]]
  def getAllJobs(filter: JobFilter, pagination: Pagination): F[List[Job]]
  def findJob(jobId: UUID): F[Option[Job]]
  def createJob(jobOwner: String, job: JobInfo): F[UUID]
  def updateJob(jobId: UUID, jobInfo: JobInfo): F[Option[Job]]
  def deleteJob(jobId: UUID): F[Int]
}
