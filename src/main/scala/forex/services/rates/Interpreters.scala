package forex.services.rates

import cats.Applicative
import cats.effect.ConcurrentEffect
import forex.config.OneFrameConfig
import forex.http.HttpClient
import interpreters._

object Interpreters {
  def dummy[F[_]: Applicative]: Algebra[F] = new OneFrameDummy[F]()
  def oneFrameService[F[_]: ConcurrentEffect]
  (httpClient: HttpClient[F], oneFrameConfig: OneFrameConfig): Algebra[F] = new OneFrameService(httpClient, oneFrameConfig)
}
