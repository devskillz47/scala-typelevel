package app.domain

object pagination {
  final case class Pagination(limit: Int, offset: Int)

  object Pagination {
    def apply(maybeLimit: Option[Int], maybeOffset: Option[Int]) =
      new Pagination(maybeLimit.getOrElse(24), maybeOffset.getOrElse(0))
  }
}
