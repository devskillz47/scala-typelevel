package app.svcs

import app.config.PostgresConfig
import app.db.JobRepoDao
import cats.effect.Async
import cats.effect.kernel.Resource
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

final case class RepoSvc[F[_]] private (jobRepoDao: JobRepoDao[F])

object RepoSvc {
  def postgresResource[F[_] : Async](config: PostgresConfig): Resource[F, HikariTransactor[F]] =
    for {
      execContext <- ExecutionContexts.fixedThreadPool[F](config.nThreads)
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        config.url,
        config.user,
        config.pwd,
        execContext
      )
    } yield xa

  def apply[F[_] : Async](config: PostgresConfig): Resource[F, RepoSvc[F]] =
    postgresResource[F](config)
      .map(tran => new RepoSvc(JobRepoDao(tran)))
}


