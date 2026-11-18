package app.db

import app.utils.JobUtils
import cats.effect.IO
import cats.effect.kernel.Resource
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.should.Matchers

class JobsRepoDaoSpec
  extends AsyncFreeSpec
    with AsyncIOSpec
    with Matchers
    with DoobieUtils
    with JobUtils {
  val initScript: String = "docker/init.sql"
  val jobRepo: Resource[IO, JobRepoDao[IO]] = transactorResource.map(xa => JobRepoDao[IO](xa))
  "JobsRepoDao" - {
    "should return a job by id" in {
      val retrieved = jobRepo.use(jobRepo => jobRepo.findJob(AwesomeJob.id))
      retrieved.asserting(_ shouldBe Some(AwesomeJob))
    }

    "should not return a job if the UUID does not exist" in {
      val retrieved = jobRepo.use(jobRepo => jobRepo.findJob(NotFoundJobUuid))
      retrieved.asserting(_ shouldBe None)
    }

    "should return all jobs" in {
      val retrieved = jobRepo.use(jobRepo => jobRepo.getAllJobs)
      retrieved.asserting(_ shouldBe List(AwesomeJob))
    }

    "should be able to create a job" in {
      val retrieved = jobRepo.use(
        jobRepo => for {
          newJobUUID <- jobRepo.createJob("testEmail@myemail.com", NewJobInfo)
          maybeJob <- jobRepo.findJob(newJobUUID)
        } yield maybeJob
      )
      retrieved.asserting(_.map(_.jobInfo) shouldBe Some(NewJobInfo))
    }

    "should be able to update an existing job" in {
      val retrieved = jobRepo.use(jobRepo => jobRepo.updateJob(AwesomeJob.id, UpdatedJobInfo))
      retrieved.asserting(_.map(_.jobInfo) shouldBe Some(UpdatedJobInfo))
    }

    "should not update a job if the UUID does not exist" in {
      val retrieved = jobRepo.use(jobRepo => jobRepo.updateJob(NotFoundJobUuid, UpdatedJobInfo))
      retrieved.asserting(_ shouldBe None)
    }

    "should be able to delete an existing job" in {
      val retrieved = jobRepo.use(
        jobRepo => for {
          rowsDeleted <- jobRepo.deleteJob(AwesomeJob.id)
          rowsLeft <- jobRepo.findJob(AwesomeJob.id)
        } yield (rowsDeleted, rowsLeft)
      )
      retrieved.asserting(_ shouldBe (1, None))
    }

    "should not delete a job if the UUID does not exist" in {
      val retrieved = jobRepo.use(
        jobRepo => for {
          rowsDeleted <- jobRepo.deleteJob(NotFoundJobUuid)
          rowsLeft <- jobRepo.findJob(NotFoundJobUuid)
        } yield (rowsDeleted, rowsLeft)
      )
      retrieved.asserting(_ shouldBe(0, None))
    }
  }
}
