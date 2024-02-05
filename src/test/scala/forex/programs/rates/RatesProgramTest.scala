package forex.programs.rates

import cats.effect.IO
import cats.implicits._
import forex.constants.ApiConstants
import forex.domain.Rate.Pair
import forex.domain.{Currency, Price, Rate}
import forex.programs.rates.Protocol.GetRatesRequest
import forex.programs.rates.errors.Error.RateLookupFailed
import forex.services.rates.errors.Error.OneFrameLookupFailed
import forex.services.rates.errors.{Error => RatesServiceError}
import forex.services.rates.interpreters.OneFrameService
import io.chrisdavenport.mules.{MemoryCache, TimeSpec}
import munit.CatsEffectSuite
import org.mockito.cats.MockitoCats.whenF
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class RatesProgramTest extends CatsEffectSuite with MockitoSugar with Matchers with ArgumentMatchersSugar {
  private val ratesService = mock[OneFrameService[IO]]

  test("get currency exchange rates from api [Success]") {
    val cache = MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(3.second))).unsafeRunSync()
    val pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
    whenF(ratesService.get(pair)) thenReturn ApiConstants.sampleRate.asRight[RatesServiceError]
    val program = new Program[IO](ratesService, cache)

    val response = program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))

    assertIO(response, ApiConstants.sampleRate.asRight[errors.Error])
  }

  test("get cached response on multiple calls [Success]") {
    val cache = MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(3.second))).unsafeRunSync()
    val pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
    val newResp = ApiConstants.sampleRate.copy(price = Price(0.987654))
    whenF(
      ratesService.get(pair)
    ) thenReturn ApiConstants.sampleRate.asRight[RatesServiceError] thenReturn newResp.asRight[RatesServiceError]
    val program = new Program[IO](ratesService, cache)

    val result = for{
      response1 <- program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))
      response2 <- program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))
    } yield response1 == response2 &&
      response2 != newResp.asRight[errors.Error] &&
      response1 == ApiConstants.sampleRate.asRight[errors.Error]

    assertIOBoolean(result)
  }

  test("get different response on multiple calls if cache data expires [Success]") {
    val cache = MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(3.second))).unsafeRunSync()
    val pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
    val newResp = ApiConstants.sampleRate.copy(price = Price(0.987654))
    whenF(
      ratesService.get(pair)
    ) thenReturn ApiConstants.sampleRate.asRight[RatesServiceError] thenReturn newResp.asRight[RatesServiceError]
    val program = new Program[IO](ratesService, cache)

    val result = for{
      response1 <- program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))
      _ <- IO.sleep(3 seconds)
      response2 <- program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))
    } yield {
      response1 != response2 &&
        response2 == newResp.asRight[errors.Error] &&
        response1 == ApiConstants.sampleRate.asRight[errors.Error]
    }

    assertIOBoolean(result)
  }

  test("in case of error from service, convert to program error and pass the response [Failure]") {
    val cache = MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(3.second))).unsafeRunSync()
    val pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
    whenF(ratesService.get(pair)) thenReturn OneFrameLookupFailed(ApiConstants.networkErrorException).asInstanceOf[RatesServiceError].asLeft[Rate]
    val program = new Program[IO](ratesService, cache)

    val response = program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))

    assertIO(response, RateLookupFailed(ApiConstants.networkErrorException).asInstanceOf[errors.Error].asLeft[Rate])
  }
}
