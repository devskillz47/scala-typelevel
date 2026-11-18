package app.db

import app.config.PostgresConfig
import cats.effect.{Async, IO, Resource}
import doobie.implicits.*
import doobie.util.*
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.testcontainers.containers.PostgreSQLContainer

trait DoobieUtils {
  val initScript: String
  val postgresDbResource: Resource[IO, PostgreSQLContainer[Nothing]] = {
    val acquire = IO {
      val cntainer: PostgreSQLContainer[Nothing] =
        new PostgreSQLContainer("postgres").withInitScript(initScript)
      cntainer.start()
      cntainer
    }
    val release = (cntainer: PostgreSQLContainer[Nothing]) => IO(cntainer.stop())
    Resource.make(acquire)(release)
  }

  val transactorResource: Resource[IO, HikariTransactor[IO]] =
    for {
      db <- postgresDbResource
      execContext <- ExecutionContexts.fixedThreadPool[IO](4)
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.postgresql.Driver",
        db.getJdbcUrl,
        db.getUsername,
        db.getPassword,
        execContext
      )
    } yield xa
}
