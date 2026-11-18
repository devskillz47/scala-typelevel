package foundations

import cats.effect.kernel.MonadCancelThrow
import cats.effect.{IO, IOApp}
import cats.syntax.all.catsSyntaxApplicativeError
import doobie.hikari.HikariTransactor
import doobie.implicits.*
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

object Doobie extends IOApp.Simple {

  case class Student(id: Int, name: String)

  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:demo",
    "postgres",
    "postgres",
    None)

  def findAllStudentNames: IO[List[String]] = {
    val query = sql"select name from students".query[String]
    val action = query.to[List]
    action.transact(xa)
  }

  def saveStudent(id: Int, name: String): IO[Int] = {
    val query = sql"insert into students values ($id, $name)"
    val action = query.update.run
    action.transact(xa)
  }

  def findStudentByInitial(letter: String): IO[List[Student]] = {
    val selPart = fr"select *"
    val fromPart = fr"from students"
    val wherePart = fr"where left(name, 1) = $letter"

    val statement = selPart ++ fromPart ++ wherePart
    val query = statement.query[Student].to[List]
    query.transact(xa)
  }

  //  code organization
  trait StudentRepo[F[_]] {
    def findStudentById(id: Int): F[Option[Student]]

    def findAllStudents(): F[List[Student]]

    def saveStudent(name: String): F[Either[Throwable, Int]]
  }

  object StudentRepo {
    def make[F[_] : MonadCancelThrow](xa: Transactor[F]): StudentRepo[F] = new StudentRepo[F] {
      def findAllStudents(): F[List[Student]] =
        sql"select * from students".query[Student].to[List].transact(xa)

      def findStudentById(id: Int): F[Option[Student]] =
        sql"select * from students where id = $id".query[Student].option.transact(xa)

      def saveStudent(name: String): F[Either[Throwable, Int]] = {
        sql"insert into students (name) values ($name)".update.withUniqueGeneratedKeys[Int]("id").transact(xa).attempt
      }
    }
  }

  val postgresResource = for {
    execContext <- ExecutionContexts.fixedThreadPool[IO](16)
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      "jdbc:postgresql:demo",
      "postgres",
      "postgres",
      execContext
    )
  } yield xa

  val smallProgram = postgresResource.use { xa =>
    val studentRepo = StudentRepo.make[IO](xa)
    for {
      studentId <- studentRepo.saveStudent("aStudent50")
      _ <- IO.println(s"New student id is: $studentId")
      maybeStudent <- studentId.fold(_ => IO.none, studentRepo.findStudentById)
      _ <- IO.println(maybeStudent match {
        case Some(s) => s"Student corresponding to id ${s.id} is: ${s.name}"
        case _ => "Student not found"
      })
    } yield ()
  }

  override def run: IO[Unit] = smallProgram
}
