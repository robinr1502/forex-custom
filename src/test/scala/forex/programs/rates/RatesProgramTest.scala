package forex.programs.rates

import cats.effect.IO
import cats.implicits.catsSyntaxEitherId
import forex.constants.ApiConstants
import forex.domain.Rate.Pair
import forex.domain.{Currency, Rate}
import forex.programs.rates.Protocol.GetRatesRequest
import forex.services.rates.errors.{ Error => RatesServiceError }
import forex.services.rates.interpreters.OneFrameService
import io.chrisdavenport.mules.{MemoryCache, TimeSpec}
import munit.CatsEffectSuite
import org.mockito.cats.MockitoCats.whenF
import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.matchers.should.Matchers

import scala.concurrent.duration.DurationInt

class RatesProgramTest extends CatsEffectSuite with MockitoSugar with Matchers with ArgumentMatchersSugar {
  private val ratesService = mock[OneFrameService[IO]]
  private val cache = MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(300.second))).unsafeRunSync()

  test("get currency exchange rates from api [Success]") {
    val pair = Pair(Currency.fromString("USD"), Currency.fromString("JPY"))
    whenF(ratesService.get(pair)) thenReturn ApiConstants.sampleRate.asRight[RatesServiceError]
    val program = new Program[IO](ratesService, cache)

    val response = program.get(GetRatesRequest(ApiConstants.samplePair.from, ApiConstants.samplePair.to))

    assertIO(response, ApiConstants.sampleRate.asRight[errors.Error])
  }
}
