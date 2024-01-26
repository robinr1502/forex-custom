package forex.http.rates

import forex.domain.Currency
import org.http4s.{ParseFailure, QueryParamDecoder}
import org.http4s.dsl.impl.ValidatingQueryParamDecoderMatcher

import scala.util.Try

object QueryParams {

  private[http] implicit val currencyQueryParam: QueryParamDecoder[Currency] = {
    QueryParamDecoder[String].emap{
      param =>
        Try(Currency.fromString(param)).toOption match {
          case None => Left(ParseFailure("Invalid query parameter", "invalid currency"))
          case Some(curr) => Right(curr)
        }
    }
  }

  object FromQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("from")
  object ToQueryParam extends ValidatingQueryParamDecoderMatcher[Currency]("to")

}
