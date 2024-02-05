package forex

import forex.domain.OneFrameRatesResponse
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe.Decoder

object Utilities {
  implicit val oneFrameRatesResponseDecoder: Decoder[OneFrameRatesResponse] =
    Decoder.forProduct6("from", "to", "bid", "ask", "price", "time_stamp")(OneFrameRatesResponse.apply)

  implicit val decodeFieldType: Decoder[OneFrameLookupFailed] =
    Decoder.forProduct1("error")(OneFrameLookupFailed.apply)

  implicit def eitherDecoder[A, B](implicit a: Decoder[A], b: Decoder[B]): Decoder[Either[A, B]] = {
    val left:  Decoder[Either[A, B]]= a.map(Left.apply)
    val right: Decoder[Either[A, B]]= b.map(Right.apply)
    left or right
  }
}
