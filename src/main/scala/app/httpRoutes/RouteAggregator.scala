package app.httpRoutes

import app.svcs.RepoSvc
import cats.data.{Kleisli, OptionT}
import cats.effect.Concurrent
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class RouteAggregator[F[_] : {Concurrent, Logger}] private (repoSvc: RepoSvc[F]) {
  private def healthRoutes = HealthRoutes[F].healthCheckRoutes

  private def jobRoutes = JobRoutes[F](repoSvc.jobRepoDao).allRoutes

  //noinspection TypeAnnotation
    val allRoutes: HttpRoutes[F] = Router {
      val routeAggRoutes = healthRoutes <+> jobRoutes
      "/api" -> routeAggRoutes
  }
}

object RouteAggregator {
  def apply[F[_] : {Concurrent, Logger}](repoSvc: RepoSvc[F]) =
    new RouteAggregator[F](repoSvc)
}
