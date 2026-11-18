package app.httpRoutes

import app.core.JobRepoAlgebra
import app.domain.Job.*
import app.httpRoutes.errorResponse.FailureResponse
import app.utils.JobUtils
import cats.effect.*
import cats.effect.testing.scalatest.AsyncIOSpec
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.{circeEntityDecoder, circeEntityEncoder}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits.uri
import org.http4s.{Method, Request, Response, Status}
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.util.UUID

class JobRoutesSpec
  extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with Http4sDsl[IO]
    with JobUtils {


  //  1. Test setup
  val jobRepo: JobRepoAlgebra[IO] = new JobRepoAlgebra[IO] {
    def getAllJobs: IO[List[Job]] = IO.pure(List(AwesomeJob))

    def findJob(jobId: UUID): IO[Option[Job]] =
      if (jobId == AwesomeJob.id) IO.pure(Some(AwesomeJob)) else IO.pure(None)

    def createJob(jobOwner: String, job: JobInfo): IO[UUID] =
      IO.pure(NewJobUuid)

    def updateJob(jobId: UUID, jobInfo: JobInfo): IO[Option[Job]] =
      if (jobId == AwesomeJob.id) {
        val UpdatedJob = AwesomeJob.copy(jobInfo = jobInfo)
        IO.pure(Some(UpdatedJob))
      } else {
        IO.pure(None)
      }

    def deleteJob(jobId: UUID): IO[Int] =
      if (jobId == AwesomeJob.id) IO.pure(1) else IO.pure(0)
  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  val jobRoutes = JobRoutes[IO](jobRepo).allRoutes

  //  2. actual tests
  "JobRoutes" - {
    "should be able return all job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/jobs/all")
        )
        retrieved <- response.as[List[Job]]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe List(AwesomeJob)
      }
    }

    "should return a job with a given id" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.GET, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        retrieved <- response.as[Job]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe AwesomeJob
      }
    }

    "should be able to create a new job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.POST, uri = uri"/jobs")
            .withEntity(NewJobInfo)
        )
        retrieved <- response.as[UUID]
      } yield {
        response.status shouldBe Status.Created
        retrieved shouldBe NewJobUuid
      }
    }

    "should be able to update an existing job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.PUT, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
            .withEntity(UpdatedJobInfo)
        )
        retrieved <- response.as[Job]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe AwesomeJob.copy(jobInfo = UpdatedJobInfo)
      }
    }

    "should not be able to update a non-existing job" in {
      for {
        response <- jobRoutes.orNotFound.run(
//        val NotFoundJobUuid = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")
          Request(method = Method.PUT, uri = uri"/jobs/6ea79557-3112-4c84-a8f5-1d1e2c300948")
            .withEntity(UpdatedJobInfo)
        )
        retrieved <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.NotFound
        retrieved shouldBe FailureResponse("Cannot find job 6ea79557-3112-4c84-a8f5-1d1e2c300948")
      }
    }

    "should be able to delete an existing job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/843df718-ec6e-4d49-9289-f799c0f40064")
        )
        retrieved <- response.as[String]
      } yield {
        response.status shouldBe Status.Ok
        retrieved shouldBe "Lines affected: 1"
      }
    }

    "should not be able to delete a non-existing job" in {
      for {
        response <- jobRoutes.orNotFound.run(
          Request(method = Method.DELETE, uri = uri"/jobs/6ea79557-3112-4c84-a8f5-1d1e2c300948")
        )
        retrieved <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.NotFound
        retrieved shouldBe FailureResponse("Cannot find job 6ea79557-3112-4c84-a8f5-1d1e2c300948")
      }
    }
  }
}

