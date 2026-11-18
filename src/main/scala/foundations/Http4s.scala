package foundations

import cats.*
import cats.data.Kleisli
import cats.effect.*
import cats.syntax.all.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.*

import java.util.UUID

object Http4s extends IOApp.Simple {
  //  simulate an http-server with students & courses
  type Student = String

  case class Instructor(firstName: String, lastName: String)

  case class Course(id: String, title: String, year: Int, students: List[Student], instructor: String)

  object CourseRepo {
    val course1 = Course(
      "14073a91-ac8f-4ec5-9459-2df03fb8dcd6",
      "RockTheJVMCatsEffectCourse",
      2022,
      List("Bob", "Alice"),
      "Martin Odersky"
    )

    val courses: Map[String, Course] = Map("14073a91-ac8f-4ec5-9459-2df03fb8dcd6" -> course1)

    //API
    def findCourseById(courseId: UUID): Option[Course] = {
      courses.get(courseId.toString)
    }

    def findCoursesByInstructor(instructorName: String): List[Course] = {
      courses.values.filter(course => course.instructor == instructorName).toList
    }

    //    REST endpoints
    // GET localhost:8080/courses?instructor=Martin%20Odersky&year=2022
    // GET localhost:8080/courses/<uuid>/students
    object InstructorQueryParamMatcher extends QueryParamDecoderMatcher[String]("instructor")

    object YearQueryParamMatcher extends OptionalValidatingQueryParamDecoderMatcher[Int]("year")

    def courseRoutes[F[_] : Monad]: HttpRoutes[F] = {
      val dsl = Http4sDsl[F]
      import dsl.*

      HttpRoutes.of[F] {
        // GET localhost:8080/courses?instructor=Martin%20Odersky&year=2022
        case GET -> Root / "courses" :? InstructorQueryParamMatcher(instrName) +& YearQueryParamMatcher(maybeYear) =>
          val courses = CourseRepo.findCoursesByInstructor(instrName)
          maybeYear match {
            case Some(y) => y.fold(
              _ => BadRequest("Year is invalid"),
              year => Ok(courses.filter(_.year == year).asJson)
            )
            case None => Ok(courses.asJson)
          }
        // GET localhost:8080/courses/<uuid>/students
        case GET -> Root / "courses" / UUIDVar(uuid) / "students" =>
          val students = CourseRepo.findCourseById(uuid).map(_.students)
          students match {
            case Some(students) => Ok(students.asJson)
            case None => NotFound(s"No course with courseId $uuid was found")
          }
      }
    }

    def healthCheck[F[_] : Monad]: HttpRoutes[F] = {
      val dsl = Http4sDsl[F]
      import dsl.*

      HttpRoutes.of[F] {
        case GET -> Root / "pingServer" => Ok("Server working!")
      }
    }

    def allRoutes[F[_] : Monad]: HttpRoutes[F] = courseRoutes <+> healthCheck

    def routerWithPathPrefixes: Kleisli[IO, Request[IO], Response[IO]] = Router(
      "api" -> courseRoutes[IO],
      "healthChecks" -> healthCheck[IO]
    ).orNotFound
  }

  import CourseRepo.*

  def run: IO[Unit] = EmberServerBuilder
    .default[IO]
    .withHttpApp(routerWithPathPrefixes)
    .build
    .use(_ => IO.println("Server ready") *> IO.never)
}
