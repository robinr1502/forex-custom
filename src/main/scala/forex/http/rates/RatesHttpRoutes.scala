package forex.http
package rates

import cats.effect.Sync
import cats.syntax.flatMap._
import forex.programs.RatesProgram
import forex.programs.rates.{ Protocol => RatesProgramProtocol }
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

class RatesHttpRoutes[F[_]: Sync](rates: RatesProgram[F]) extends Http4sDsl[F] {

  import Converters._, QueryParams._, Protocol._

  private[http] val prefixPath = "/rates"

  private val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root :? FromQueryParam(from) +& ToQueryParam(to) =>
      val fromValid = from.fold(
        _ => None,
        from => Some(from)
      )
      val toValid = to.fold(
        _ => None,
        to => Some(to)
      )
      if (fromValid.isDefined && toValid.isDefined) {
        rates.get(RatesProgramProtocol.GetRatesRequest(fromValid.get, toValid.get)).flatMap {
          case Left(err) => InternalServerError(ErrorResponse(err.getMsg))
          case Right(rate) => Ok(rate.asGetApiResponse)
        }
      } else {
        BadRequest(ErrorResponse("unable to parse query arguments"))
      }
    case _ -> Root => BadRequest(ErrorResponse("invalid route or data parameters"))
  }

  val routes: HttpRoutes[F] = Router(
    prefixPath -> httpRoutes
  )

}
