package forex

import cats.effect.{ConcurrentEffect, Timer}
import forex.config.ApplicationConfig
import forex.domain.Rate
import forex.http.rates.RatesHttpRoutes
import forex.services._
import forex.programs._
import io.chrisdavenport.mules.MemoryCache
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.{AutoSlash, Timeout}

import scala.concurrent.ExecutionContext

class Module[F[_]: ConcurrentEffect: Timer](config: ApplicationConfig, cache: MemoryCache[F, String, Rate]) {

  private val client = BlazeClientBuilder[F](ExecutionContext.global)

  private val ratesService: RatesService[F] = RatesServices.oneFrameService[F](client, config.oneframe)

  private val ratesProgram: RatesProgram[F] = RatesProgram[F](ratesService, cache)

  private val ratesHttpRoutes: HttpRoutes[F] = new RatesHttpRoutes[F](ratesProgram).routes

  type PartialMiddleware = HttpRoutes[F] => HttpRoutes[F]
  type TotalMiddleware   = HttpApp[F] => HttpApp[F]

  private val routesMiddleware: PartialMiddleware = {
    { http: HttpRoutes[F] =>
      AutoSlash(http)
    }
  }

  private val appMiddleware: TotalMiddleware = { http: HttpApp[F] =>
    Timeout(config.http.timeout)(http)
  }

  private val http: HttpRoutes[F] = ratesHttpRoutes

  val httpApp: HttpApp[F] = appMiddleware(routesMiddleware(http).orNotFound)

}
