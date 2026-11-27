package app.httpRoutes.validation

import app.domain.job.JobInfo
import cats.data.ValidatedNel
import cats.implicits.{catsSyntaxTuple14Semigroupal, catsSyntaxValidatedId}

import java.net.URI
import scala.util.{Failure, Success, Try}

object validators {

  sealed trait ValidationFailure(err: String)

  //empty field, invalid URL, invalid email, etc
  case class EmptyField(fieldName: String) extends ValidationFailure(s"$fieldName should not be empty")

  case class InvalidUrl(fieldName: String, validationError: String)
    extends ValidationFailure(s"$fieldName is not a valid url: $validationError")

  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  trait Validator[A] {
    def validate(aVal: A): ValidationResult[A]
  }

  def validateNonEmpty[A](fieldValue: A, fieldName: String): ValidationResult[A] = {
    if (fieldValue.toString.nonEmpty) {
      fieldValue.validNel
    } else EmptyField(fieldName).invalidNel
  }

  def validateUrl(fieldValue: String, fieldName: String): ValidationResult[String] = {
    Try(URI.create(fieldValue).toURL) match {
      case Success(url) => fieldValue.validNel
      case Failure(error) => InvalidUrl(fieldValue, error.getMessage).invalidNel
    }
  }

  //  unpacking/deconstructing a tuple into individual args
  given jobInfoValidator: Validator[JobInfo] = (jobInfo: JobInfo) => {
    val JobInfo(
      company, // not empty
      image,
      title, // not empty
      desc, // not empty
      remote, // not empty
      externalUrl, // not empty
      salaryHigh,
      salaryLow,
      currency,
      location,
      country,
      tags,
      seniority,
      other) = jobInfo

    val validatedCompany = validateNonEmpty(company, "company")
    val validatedTitle = validateNonEmpty(title, "title")
    val validatedDesc = validateNonEmpty(desc, "desc")
    val validatedRemote = validateNonEmpty(remote, "remote")
    val validatedExternalUrl = validateUrl(externalUrl, "externalUrl")

    //    repacking/reconstructing an obj from a tuple (ie. using mapN)
    (validatedCompany,
      image.validNel,
      validatedTitle,
      validatedDesc,
      validatedRemote,
      validatedExternalUrl,
      salaryHigh.validNel,
      salaryLow.validNel,
      currency.validNel,
      location.validNel,
      country.validNel,
      tags.validNel,
      seniority.validNel,
      other.validNel).mapN(JobInfo.apply)
  }
}

