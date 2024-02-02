package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    case class OneFrameLookupFailed(error: String) extends Error
  }

}
