import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.http.HttpClient.{Redirect, Version}
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.jdk.FutureConverters.CompletionStageOps
import scala.jdk.OptionConverters.RichOptional
import scala.concurrent.duration._

object UrlResolver extends App {
  val client = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.NEVER)
    .build()

  private def getHeadResponse(client: HttpClient, uri: URI): Future[HttpResponse[Void]] = {
    val request = HttpRequest.newBuilder()
      .uri(uri)
      .timeout(Duration.ofMinutes(2))
      .header("Content-Type", "application/json")
      .method("HEAD", BodyPublishers.noBody())
      .build()

    client.sendAsync(request, BodyHandlers.discarding()).asScala
  }

  def resolve(uri: URI, count: Int = 10): Future[Either[String, URI]] = {
    println(s"resolve $uri, with count $count")
    if (count == 0) {
      Future.successful(Left("too many redirects"))
    } else {
      (for {
        response <- getHeadResponse(client, uri)
      } yield {
        response.headers().firstValue("Location").toScala.fold(Future.successful[Either[String, URI]](Right(uri))) { location =>
          resolve(uri.resolve(location), count - 1)
        }
//        for {
//          location <- response.headers().firstValue("Location").toScala
//        } yield {
//          println(s"yield $location")
//          resolve(uri.resolve(location), count - 1)
//        }
      }).flatten
//        .getOrElse(Future.successful(Right(uri)))).flatten
    }
  }


  println(Await.result(resolve(URI.create("http://localhost:8000/redirect-ping")), scala.concurrent.duration.Duration(10, SECONDS)))
}
