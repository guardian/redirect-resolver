import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.{Header, HttpRoutes, Uri}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Location
import org.http4s.server.Router
import org.http4s.implicits._

import scala.concurrent.ExecutionContext

class PingApi extends Http4sDsl[IO] {
  val routes = HttpRoutes.of[IO] {
    case GET -> Root / "ping" => Ok("pong")
    case (GET | HEAD) -> Root / "redirect" => MovedPermanently("Moved Permanently", Location(uri"/ping"))
    case (GET | HEAD) -> Root / "redirect-ping" => MovedPermanently("Moved Permanently", Location(uri"/redirect-pong"))
    case (GET | HEAD) -> Root / "redirect-pong" => MovedPermanently("Moved Permanently", Location(uri"/redirect-ping"))
  }
}

object PingApp extends IOApp {
  private val httpApp = Router(
    "/" -> new PingApi().routes
  ).orNotFound
  override def run(args: List[String]): IO[ExitCode] =
    stream(args).compile.drain.as(ExitCode.Success)
  private def stream(args: List[String]): fs2.Stream[IO, ExitCode] =
    BlazeServerBuilder[IO](ExecutionContext.global)
      .bindHttp(8000, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
}