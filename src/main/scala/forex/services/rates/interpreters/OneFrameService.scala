package forex.services.rates.interpreters

import cats.effect.Concurrent.ops.toAllConcurrentOps
import cats.effect.ConcurrentEffect
import cats.implicits._
import forex.config.OneFrameConfig
import forex.domain.{OneFrameRatesResponse, Price, Rate, Timestamp}
import forex.services.rates.Algebra
import forex.Utilities._
import forex.services.rates.errors.Error
import forex.services.rates.errors.Error.OneFrameLookupFailed
import io.circe.Decoder
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.{EntityDecoder, Header, Headers, Method, Request, Uri}
import org.typelevel.ci.CIString


class OneFrameService[F[_]: ConcurrentEffect](
                                           client: BlazeClientBuilder[F],
                                           oneFrameConfig: OneFrameConfig
                                         ) extends Algebra[F] {

  override def get(pair: Rate.Pair): F[Error Either Rate] = {
    client.resource.use{ httpClient =>
      val url = s"http://${oneFrameConfig.http.host}:${oneFrameConfig.http.port}/rates?pair=${pair.from.show.concat(pair.to.show)}"
      val getRatesUri = Uri.fromString(url).toOption
      getRatesUri match {
        case None => Left(OneFrameLookupFailed("Invalid url provided")).asInstanceOf[Error Either Rate].pure[F]
        case Some(uri) => sendReq(pair, uri, httpClient)
      }
    }
  }

  private def sendReq(pair: Rate.Pair, uri: Uri, httpClient: Client[F]): F[Error Either Rate] = {
    implicit val reqDecoder: Decoder[Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]] =
      eitherDecoder[OneFrameLookupFailed, List[OneFrameRatesResponse]]
    implicit val reqEncoder: EntityDecoder[F, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]] =
      jsonOf[F, Either[OneFrameLookupFailed, List[OneFrameRatesResponse]]]
    val request = Request[F](
      method = Method.GET,
      uri = uri,
      headers = Headers(List(Header.Raw(CIString("token"),
        oneFrameConfig.token)))
    )
    val req = httpClient.expect(request).handleError {
      error: Throwable =>
        println("Exception: " + error.getMessage)
        Left(OneFrameLookupFailed(error.getMessage))
    }
    for{
      fib <- req.start
      res <- fib.join
    } yield{
      res match {
        case Left(error) => Left(error)
        case Right(data) =>
          data.headOption match {
            case None => Left(OneFrameLookupFailed("Empty response")).asInstanceOf[Error Either Rate]
            case Some(data) => Right(
              Rate(pair, Price(data.price), Timestamp.fromStringDate(data.time_stamp, "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").getOrElse(Timestamp.now))
            ).asInstanceOf[Error Either Rate]
          }
      }
    }
  }
}