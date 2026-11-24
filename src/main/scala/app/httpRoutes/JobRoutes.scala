package app.httpRoutes

import app.core.JobRepoAlgebra
import app.domain.Job.{Job, JobInfo}
import app.httpRoutes.errorResponse.FailureResponse
import app.httpRoutes.validation.syntax.*
import cats.data.Validated.*
import cats.effect.*
import cats.syntax.all.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.*
import org.http4s.server.*
import org.typelevel.log4cats.Logger

import java.util.UUID

class JobRoutes[F[_] : {Concurrent, Logger}] private(jobRepoAlgebra: JobRepoAlgebra[F]) extends Http4sDsl[F] {

  private def getAllJobs: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "all" =>
        Ok(jobRepoAlgebra.getAllJobs)
    }
  }

  private def findJob: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / UUIDVar(jobId) =>
        jobRepoAlgebra.findJob(jobId).flatMap {
          case Some(job) => Ok(job.asJson)
          case None => NotFound(FailureResponse(s"Job $jobId not found").asJson)
        }
    }
  }

  private def createJob: HttpRoutes[F] = {
    val ownerEmail = "aUser@jobsPortal.com"
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        req.validate[JobInfo].flatMap {
          case Valid(jobInfo) =>
            for {
              jobId <- jobRepoAlgebra.createJob(ownerEmail, jobInfo)
              status <- Created(jobId)
            } yield status
          case Invalid(error) => BadRequest(FailureResponse(error.toString))
        }
    }
  }

  private def updateJob: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@PUT -> Root / UUIDVar(jobId) =>
        req.validate[JobInfo].flatMap {
          case Valid(jobInfo) =>
            for {
              maybeNewJob <- jobRepoAlgebra.updateJob(jobId, jobInfo)
              status <-
                maybeNewJob match {
                  case Some(job) => Ok(job)
                  case None => NotFound(FailureResponse(s"Cannot find job $jobId"))
                }
            } yield status
          case Invalid(error) => BadRequest(FailureResponse(error.toString))
        }
    }
  }

  private def deleteJob: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case DELETE -> Root / UUIDVar(jobId) =>
        jobRepoAlgebra.findJob(jobId).flatMap {
          case Some(job) =>
            for {
              linesAffected <- jobRepoAlgebra.deleteJob(jobId)
              status <- Ok(s"Lines affected: $linesAffected")
            } yield status
          case None => NotFound(FailureResponse(s"Cannot find job $jobId"))
        }
    }
  }

  def allRoutes: HttpRoutes[F] = Router[F] {
    val allJobRoutes = getAllJobs <+> findJob <+> createJob <+> updateJob <+> deleteJob
    "jobs" -> allJobRoutes
  }
}

object JobRoutes {
  def apply[F[_] : {Concurrent, Logger}](jobRepo: JobRepoAlgebra[F]) = new JobRoutes[F](jobRepo)
}

