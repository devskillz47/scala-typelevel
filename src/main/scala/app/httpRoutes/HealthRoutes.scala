package app.httpRoutes

import cats.Monad
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router


class HealthRoutes[F[_] : Monad] private extends Http4sDsl[F] {
  private def healthCheck: HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "pingServer" => Ok("Server working!")
    }
  }

  def healthCheckRoutes: HttpRoutes[F] = Router[F](
    "healthChecks" -> healthCheck
  )
}

object HealthRoutes {
  def apply[F[_] : Monad] = new HealthRoutes[F]
}
