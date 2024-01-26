package forex.services.rates

object errors {

  sealed trait Error
  object Error {
    case class OneFrameLookupFailed(msg: String) extends Error
  }

}
