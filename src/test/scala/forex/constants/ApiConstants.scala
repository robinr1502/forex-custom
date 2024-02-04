package forex.constants

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import forex.config.{HttpConfig, OneFrameConfig}
import forex.domain.Rate.Pair
import forex.domain.{Currency, OneFrameRatesResponse, Price, Rate, Timestamp}
import forex.services.rates.errors.Error.OneFrameLookupFailed
import org.http4s.{Header, Headers, Method, Request, Uri}
import org.typelevel.ci.CIString

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object ApiConstants {
  val oneFrameConfig: OneFrameConfig = OneFrameConfig(HttpConfig("localhost", 8080, 40.second), "10dc303535874aeccc86a8251e6992f5")
  val validRatesUri: Option[Uri] = Uri.fromString("http://localhost:8080/rates?pair=USDJPY").toOption
  val networkErrorException = "Error connecting to http://localhost:8080 using address localhost:8080 (unresolved: false)"
  val validRatesGetRequest: Request[IO] = Request[IO](
    method = Method.GET,
    uri = validRatesUri.get,
    headers = Headers(List(Header.Raw(CIString("token"),
      oneFrameConfig.token)))
  )
  val sampleTime: String = OffsetDateTime.now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
  val samplePair: Pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
  val sampleRate: Rate = Rate(samplePair, Price(0.214312), Timestamp.fromStringDate(ApiConstants.sampleTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").get)
  val successRatesGetResponse: Either[OneFrameLookupFailed, List[OneFrameRatesResponse]] = List(
      OneFrameRatesResponse("USD","JPY", 0.131241, 0.243245, 0.214312, sampleTime)
    ).asRight[OneFrameLookupFailed]
  val emptyGetRatesResponse: Either[OneFrameLookupFailed, List[OneFrameRatesResponse]] = List.empty.asRight[OneFrameLookupFailed]
  val invalidCurrencyFailure: Either[OneFrameLookupFailed, List[OneFrameRatesResponse]] = OneFrameLookupFailed(
    "Invalid currency"
  ).asLeft[List[OneFrameRatesResponse]]
}
