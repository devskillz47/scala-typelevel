package app.core

import app.domain.Job.{Job, JobInfo}

import java.util.UUID

trait JobRepoAlgebra[F[_]] {
  def getAllJobs: F[List[Job]]
  def findJob(jobId: UUID): F[Option[Job]]
  def createJob(jobOwner: String, job: JobInfo): F[UUID]
  def updateJob(jobId: UUID, jobInfo: JobInfo): F[Option[Job]]
  def deleteJob(jobId: UUID): F[Int]
}
