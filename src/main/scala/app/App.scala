package app

import app.config.AppConfig
import app.httpRoutes.RouteAggregator
import app.logging.syntax.*
import app.svcs.RepoSvc
import cats.*
import cats.effect.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.*
import pureconfig.module.catseffect.syntax.*

object App extends IOApp.Simple {

  given logger: Logger[IO] = Slf4jLogger.getLogger

  def run: IO[Unit] = {
    val config = ConfigSource.file("src/main/resources/application.conf").loadF[IO, AppConfig]()
    config.flatMap {
      case AppConfig(postgresConfig, emberConfig) =>
        val appResource = for {
          repoSvcs <- RepoSvc[IO](postgresConfig)
          allRoutes = RouteAggregator[IO](repoSvcs).allRoutes.logRouteErrors(err => s"Could not process request: $err")
          server <- EmberServerBuilder
            .default[IO]
            .withHost(emberConfig.host)
            .withPort(emberConfig.port)
            .withHttpApp(allRoutes.orNotFound)
            .build
        } yield server
        appResource.use(_ => IO.println("Server ready") *> IO.never)
    }


  }

}



