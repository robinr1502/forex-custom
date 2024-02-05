package forex.constants

import cats.effect.IO
import forex.Utilities.{decodeFieldType, eitherDecoder, oneFrameRatesResponseDecoder}
import forex.domain.OneFrameRatesResponse
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe.Decoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf

object ImplicitConstants {
  implicit val reqDecoder: Decoder[Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]] =
    eitherDecoder[OneFrameLookupFailed, List[OneFrameRatesResponse]]
  implicit val reqEncoder: EntityDecoder[IO, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]] =
    jsonOf[IO, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]]
}
