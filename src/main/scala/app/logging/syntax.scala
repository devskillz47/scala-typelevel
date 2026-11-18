package app.logging

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import cats.effect.MonadCancel
import cats.implicits.{catsSyntaxMonadError, *}
import org.http4s.HttpRoutes
import org.http4s.dsl.io.InternalServerError
import org.typelevel.log4cats.Logger

object syntax {
  extension [F[_], E, A](fa: F[A])(using me: MonadError[F, E], logger: Logger[F]) {
    def log(success: A => String, error: E => String): F[A] = fa.attemptTap {
      case Left(e) => logger.error(error(e))
      case Right(a) => logger.info(success(a))
    }

    def logError(error: E => String): F[A] = fa.attemptTap {
      case Left(e) => logger.error(error(e))
      case Right(_) => ().pure
    }
  }

  extension [F[_]](routes: HttpRoutes[F])(using logger: Logger[F], F: MonadCancel[F, Throwable]) {
    def logRouteErrors(errorMsg: Throwable => String): HttpRoutes[F] = {
      Kleisli { req =>
        OptionT(
          routes(req).value.attemptTap {
            case Left(err) => logger.error(err)(errorMsg(err))
            case Right(_) => ().pure
          }
        )
      }
    }
  }
}
