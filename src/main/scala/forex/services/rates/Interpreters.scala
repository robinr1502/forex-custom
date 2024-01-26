package forex.services.rates

import cats.Applicative
import cats.effect.ConcurrentEffect
import forex.config.OneFrameConfig
import interpreters._
import org.http4s.blaze.client.BlazeClientBuilder

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def oneFrameService[F[_]: ConcurrentEffect]
  (httpClient: BlazeClientBuilder[F], oneFrameConfig: OneFrameConfig): Algebra[F] = new OneFrameService(httpClient, oneFrameConfig)
}
