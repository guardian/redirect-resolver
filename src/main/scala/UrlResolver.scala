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
import com.github.blemale.scaffeine.{ AsyncLoadingCache, Scaffeine }


object UrlResolver extends App {
  val client = HttpClient.newBuilder()
    .version(Version.HTTP_1_1)
    .followRedirects(Redirect.NEVER)
    .build()

  val cache: AsyncLoadingCache[URI, Option[URI]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture((i: URI) => follow(i))

  private def getHeadResponse(uri: URI): Future[HttpResponse[Void]] = {
    println(s"Hitting URI: $uri")
    val request = HttpRequest.newBuilder()
      .uri(uri)
      .timeout(Duration.ofMinutes(2))
      .header("Content-Type", "application/json")
      .method("HEAD", BodyPublishers.noBody())
      .build()

    client.sendAsync(request, BodyHandlers.discarding()).asScala
  }

  def follow(uri: URI): Future[Option[URI]] = {
    for {
      response <- getHeadResponse(uri)
    } yield {
      response.headers().firstValue("Location").toScala.map(URI.create)
    }
  }
  def resolve(uri: URI, count: Int = 10): Future[Either[String, URI]] = {
    println(s"resolve $uri, with count $count")
    if (count == 0) {
      Future.successful(Left("too many redirects"))
    } else {
      (for {
        response <- cache.get(uri)
      } yield response.fold(Future.successful[Either[String, URI]](Right(uri))) { location =>
        resolve(uri.resolve(location), count - 1)
        }).flatten
    }
  }

  println(Await.result(resolve(URI.create("http://localhost:8000/redirect-a")), 10.seconds))
  println(Await.result(resolve(URI.create("http://localhost:8000/redirect-b")), 10.seconds))
}
