package forex.http

import org.http4s.{EntityDecoder, Request}

trait Client[F[_]] {
  def sendReq[A](req: Request[F])(implicit d: EntityDecoder[F, A]): F[A]
}
