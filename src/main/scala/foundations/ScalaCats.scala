package foundations

import cats.syntax.flatMap.*

object ScalaCats {


// 1. Functor:
// - describes any category of objects which are "mappable"
  import cats.Functor
  val listFnctor = Functor[List]
  val aList = List(1,2,3)
  val mappedList: List[Int] = listFnctor.map(aList)(_ + 1)

  // show the ability to create generalizable "instances" without the need to define them
  // individually (ie. diff. implementations for List[Int], Future[Int], Try[Int], etc)

  def increment[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    functor.map(container)(_ + 1)

  import cats.syntax.all.toFunctorOps
  def incrementV2[F[_]](container: F[Int])(using functor: Functor[F]): F[Int] =
    container.map(_ + 2)

//  2. Applicative:
//    - implements the pure transforms (ie. A -> F[A])
  import cats.Applicative
  val applicativeList = Applicative[List]
  val aSimpleList: List[Int] = applicativeList.pure(42)

  import cats.syntax.applicative.catsSyntaxApplicativeId
  val aSimpleListV2: List[Int] = 42.pure[List]

//  3.  FlatMap:
//  - flattens the hierarchy
  import cats.FlatMap
  val flatMapList = FlatMap[List]
  val flatMappedList: List[Int] = flatMapList.flatMap(List(1,2,3))(x => List(x))

  import cats.syntax.flatMap.*
  def crossProduct[F[_]: FlatMap, A, B](containerA: F[A], containerB: F[B]): F[(A, B)] =
    containerA.flatMap(a => containerB.map(b => (a, b)))

  def crossProductV2[F[_]: FlatMap, A, B](cntnrA: F[A], cntnrB: F[B]): F[(A, B)] =
    for {
      a <- cntnrA
      b <- cntnrB
    } yield (a, b)

//  4. ApplicativeError:
//    - stores "computations" which could throw an error

  import cats.ApplicativeError
  type ErrorOr[A] = Either[String, A]
  val appError = ApplicativeError[ErrorOr, String]
  val desiredVal = appError.pure(42)
  val failedValue = appError.raiseError("Something bad happened")

  import cats.syntax.applicativeError.*
  val failedValueV2 = "Something bad happened".raiseError

//    5. MonadError:
//      - like ApplicativeError but more "powerful"

  import cats.MonadError
  val monadError = MonadError[ErrorOr, String]
//  - can be used to implement error handling for map/flatMap or pure ops

  def main(args: Array[String]): Unit = {
    println("Loading:")
    println(monadError)
    println("Finised!")
  }
}
