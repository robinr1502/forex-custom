package forex

import scala.concurrent.ExecutionContext
import cats.effect._
import forex.config._
import forex.domain.Rate
import fs2.Stream
import io.chrisdavenport.mules.{MemoryCache, TimeSpec}
import org.http4s.blaze.server.BlazeServerBuilder

import scala.concurrent.duration.DurationInt

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    for{
      cache <- MemoryCache.ofConcurrentHashMap[IO, String, Rate](Some(TimeSpec.unsafeFromDuration(300.second)))
      response <- new Application[IO].stream(executionContext, cache).compile.drain.as(ExitCode.Success)
    } yield response
  }


}

class Application[F[_]: ConcurrentEffect: Timer] {

  def stream(ec: ExecutionContext, cache: MemoryCache[F, String, Rate]): Stream[F, Unit] =
    for {
      config <- Config.stream("app")
      module = new Module[F](config, cache)
      _ <- BlazeServerBuilder[F](ec)
            .bindHttp(config.http.port, config.http.host)
            .withHttpApp(module.httpApp)
            .serve
    } yield ()

}
