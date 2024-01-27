package forex.programs.rates

import cats.effect.Concurrent
import cats.implicits.{catsSyntaxApplicativeId, catsSyntaxEitherId, toFlatMapOps, toFunctorOps}
import errors._
import forex.domain.Rate.Pair
import forex.domain._
import forex.services.RatesService
import io.chrisdavenport.mules.MemoryCache

class Program[F[_]: Concurrent](
    ratesService: RatesService[F],
    cache: MemoryCache[F, String, Rate]
) extends Algebra[F] {

  override def get(request: Protocol.GetRatesRequest): F[Error Either Rate] = {
    val key = Currency.toString(request.from) + Currency.toString(request.to)
    for{
      data <- cache.lookup(key)
      res <- data match {
        case None => getResponseAndUpdateCache(Pair(request.from, request.to), key)
        case Some(data) => data.asRight[Error].pure[F]
      }
    } yield res
  }
  private def getResponseAndUpdateCache(pair: Rate.Pair, key: String): F[Error Either Rate] = {
    for{
      apiRes <- ratesService.get(Pair(pair.from, pair.to))
      resp <- apiRes match {
        case Left(err) => toProgramError(err).asLeft[Rate].pure[F]
        case Right(rate) => rate.asRight[Error].pure[F]
      }
      _ <- if(resp.isRight) cache.insert(key, resp.toOption.get) else ().pure[F]
    } yield resp
  }

}

object Program {

  def apply[F[_]: Concurrent](
      ratesService: RatesService[F],
      cache: MemoryCache[F, String, Rate]
  ): Algebra[F] = new Program[F](ratesService, cache)

}
