package app.utils

import app.domain.job.{Job, JobInfo}
import cats.syntax.all.*
import org.scalactic.Prettifier.default

import java.util.UUID

trait JobUtils {

  val NotFoundJobUuid = UUID.fromString("6ea79557-3112-4c84-a8f5-1d1e2c300948")

  private val AwesomeJobUuid = UUID.fromString("843df718-ec6e-4d49-9289-f799c0f40064")
  val AwesomeJob = Job(
    AwesomeJobUuid,
    1659186086L,
    "daniel@rockthejvm.com",
    JobInfo(
      "Awesome Company",
      None,
      "An awesome job in Berlin",
      "Tech Lead",
      false,
      "https://rockthejvm.com/awesomejob",
      2000.some,
      3000.some,
      "EUR".some,
      "Berlin".some,
      "Germany".some,
      Some(List("scala", "scala-3", "cats")),
      "Senior".some,
      None
    ),
    false
  )

  val InvalidJob = Job(
    null,
    42L,
    "nothing@gmail.com",
    JobInfo.empty,
    false
  )

  val UpdatedJobInfo = JobInfo(
      "Awesome Company (Spain Branch)",
      "http://www.awesome.com/logo.png".some,
      "An awesome job in Barcelona",
      "Engineering Manager",
      false,
      "http://www.awesome.com",
      2200.some,
      3200.some,
      "USD".some,
      "Barcelona".some,
      "Spain".some,
      Some(List("scala", "scala-3", "zio")),
      "Highest".some,
      "Some additional info".some
    )

  val RockTheJvmNewJobInfo = JobInfo(
    "RockTheJvm",
    None,
    "For the glory of the RockTheJvm!",
    "Technical Author",
    true,
    "https://rockthejvm.com/",
    2000.some,
    3500.some,
    "EUR".some,
    "From remote".some,
    "Romania".some,
    Some(List("scala", "scala-3", "cats", "akka", "spark", "flink", "zio")),
    None,
    "High".some
  )

  val RockTheJvmJobWithNotFoundId = AwesomeJob.copy(id = NotFoundJobUuid)

  val AnotherAwesomeJobUuid = UUID.fromString("19a941d0-aa19-477b-9ab0-a7033ae65c2b")
  val AnotherAwesomeJob = AwesomeJob.copy(id = AnotherAwesomeJobUuid)

  val RockTheJvmAwesomeJob =
    AwesomeJob.copy(jobInfo = AwesomeJob.jobInfo.copy(company = "RockTheJvm"))

  val NewJobUuid = UUID.fromString("efcd2a64-4463-453a-ada8-b1bae1db4377")
  val NewJobInfo = JobInfo(
    "Awesome Company",
    None,
    "An awesome job in Berlin",
    "Tech Lead",
    false,
    "https://example.com",
    2000.some,
    3000.some,
    "EUR".some,
    "Berlin".some,
    "Germany".some,
    Some(List("scala", "scala-3", "cats")),
    "High".some,
    None
  )
}