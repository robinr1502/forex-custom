package forex.http

import cats.effect.ConcurrentEffect
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.{EntityDecoder, Request}

case class HttpClient[F[_]: ConcurrentEffect](clientResource: BlazeClientBuilder[F]){
  def sendReq[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[A] = {
    clientResource.resource.use{ client =>
      client.expect(req)
    }
  }
}
