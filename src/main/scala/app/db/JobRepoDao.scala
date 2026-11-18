package app.db

import app.core.JobRepoAlgebra
import app.domain.Job.{Job, JobInfo}
import cats.effect.kernel.MonadCancelThrow
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.implicits.*
import doobie.util.*

import java.util.UUID

class JobRepoDao[F[_] : MonadCancelThrow](xa: Transactor[F]) extends JobRepoAlgebra[F] {
  def getAllJobs: F[List[Job]] = {
    sql"SELECT * from jobRepo".query[Job].to[List].transact(xa)
  }

  def findJob(jobId: UUID): F[Option[Job]] = {
    val sqlStmnt = sql"SELECT * from jobRepo where id = $jobId"
    sqlStmnt.query[Job].option.transact(xa)
  }

  def createJob(ownerEmail: String, jobInfo: JobInfo): F[UUID] = {
    val sqlStmnt = {
      //    might need to remove "id" from the sql query as it is inserted at the end
      sql"INSERT INTO jobRepo (postDate, ownerEmail, company, image, " ++
        sql"title, description, remote, externalUrl, salaryHigh, salaryLow, currency," ++
        sql"location, country, tags, seniority, other, active) values (" ++
        sql"${System.currentTimeMillis()}, $ownerEmail, ${jobInfo.company}, " ++
        sql"${jobInfo.image}, ${jobInfo.title}, ${jobInfo.desc}, ${jobInfo.remote}, " ++
        sql"${jobInfo.externalUrl}, ${jobInfo.salaryHigh}, ${jobInfo.salaryLow}, " ++
        sql"${jobInfo.currency}, ${jobInfo.location}, ${jobInfo.country}, " ++
        sql"${jobInfo.tags}, ${jobInfo.seniority}, ${jobInfo.other}, false)"
    }
    sqlStmnt.update.withUniqueGeneratedKeys[UUID]("id").transact(xa)
  }

  def updateJob(jobId: UUID, jobInfo: JobInfo): F[Option[Job]] = {
    val updateStmnt = {
      sql"""
        update jobRepo
        set company = ${jobInfo.company},
            image = ${jobInfo.image},
            title = ${jobInfo.title},
            description = ${jobInfo.desc},
            remote = ${jobInfo.remote},
            externalUrl = ${jobInfo.externalUrl},
            salaryHigh = ${jobInfo.salaryHigh},
            salaryLow = ${jobInfo.salaryLow},
            currency = ${jobInfo.currency},
            location = ${jobInfo.location},
            country = ${jobInfo.country},
            tags = ${jobInfo.tags},
            seniority = ${jobInfo.seniority},
            other = ${jobInfo.other}
        where id = $jobId
        """
    }
    updateStmnt.update.run.transact(xa).flatMap(_ => findJob(jobId))
  }

  def deleteJob(jobId: UUID): F[Int] = {
    val delStmnt = {
      sql"""
         DELETE FROM jobRepo
         where id = $jobId
         """
    }
    delStmnt.update.run.transact(xa)
  }
}

object JobRepoDao {
  def apply[F[_] : MonadCancelThrow](xa: Transactor[F]) = new JobRepoDao(xa)
}

