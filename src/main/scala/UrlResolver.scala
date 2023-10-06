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

  def resolve(uri: URI): Future[URI] = {
    (for {
      response <- getHeadResponse(client, uri)
    } yield {
      for {
      location <- response.headers().firstValue("Location").toScala
      locationUri = uri.resolve(location)
    } yield
        resolve(locationUri)
    }.getOrElse(Future(uri))).flatten
  }


  println(Await.result(resolve(URI.create("https://www.bbc.co.uk/news/uk-wales-66956342")), scala.concurrent.duration.Duration(1, SECONDS)))
}
