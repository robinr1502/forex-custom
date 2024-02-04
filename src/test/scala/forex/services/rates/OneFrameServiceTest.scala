package forex.services.rates

import cats.effect.IO
import forex.constants.ApiConstants
import forex.domain.{OneFrameRatesResponse, Price, Rate, Timestamp}
import forex.http.HttpClient
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.interpreters.OneFrameService
import munit.CatsEffectSuite
import org.http4s.{EntityDecoder, Request}
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.mockito.cats.MockitoCats.whenF
import org.scalatest.matchers.should.Matchers

class OneFrameServiceTest extends CatsEffectSuite with MockitoSugar with Matchers with ArgumentMatchersSugar {
  private val config = ApiConstants.oneFrameConfig
  private val httpClient = mock[HttpClient[IO]]

  test("get currency exchange rates from api [Success]") {
    whenF(httpClient.sendReq(any[Request[IO]])(any[EntityDecoder[IO, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]]])) thenReturn ApiConstants.successRatesGetResponse
    val service = new OneFrameService[IO](httpClient, config)

    val response = service.get(ApiConstants.samplePair)

    assertIO(response, Right(Rate(ApiConstants.samplePair, Price(0.214312), Timestamp.fromStringDate(ApiConstants.sampleTime, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").get)))
  }

  test("get successful error response from api [Failure]") {
    whenF(httpClient.sendReq(any[Request[IO]])(any[EntityDecoder[IO, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]]])) thenReturn ApiConstants.invalidCurrencyFailure
    val service = new OneFrameService[IO](httpClient, config)

    val response = service.get(ApiConstants.samplePair)

    assertIO(response, Left(OneFrameLookupFailed("Invalid currency")))
  }

  test("get empty response from api [Failure]") {
    whenF(httpClient.sendReq(any[Request[IO]])(any[EntityDecoder[IO, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]]])) thenReturn ApiConstants.emptyGetRatesResponse
    val service = new OneFrameService[IO](httpClient, config)

    val response = service.get(ApiConstants.samplePair)

    assertIO(response, Left(OneFrameLookupFailed("Empty response")))
  }
}
