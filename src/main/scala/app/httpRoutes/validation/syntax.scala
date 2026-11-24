package app.httpRoutes.validation

import app.httpRoutes.validation.validators.{ValidationResult, Validator}
import cats.implicits.*
import cats.MonadThrow
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.{EntityDecoder, Request, Response}

object syntax {
  def validateEntity[A](entity: A)(using validator: Validator[A]): ValidationResult[A] = validator.validate(entity)

  extension [F[_]: MonadThrow](req: Request[F])
    def validate[A: Validator](using entityDecoder: EntityDecoder[F, A]): F[ValidationResult[A]] =
      req.as[A].map(validateEntity)
}
