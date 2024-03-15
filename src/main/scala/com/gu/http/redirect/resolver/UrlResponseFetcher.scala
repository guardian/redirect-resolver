package com.gu.http.redirect.resolver

import com.gu.http.redirect.resolver.UrlResponseFetcher.HttpResponseSummary
import com.gu.http.redirect.resolver.UrlResponseFetcher.HttpResponseSummary.{HTTPRedirectStatusCodes, LocationHeader}

import java.net.URI
import java.net.http.HttpClient.Version
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._
import scala.util.Success

/**
 * If you want to use an alternative HTTP client (lke OkHttp), you can implement this trait and pass it to
 * UrlResolver.
 */
trait UrlResponseFetcher {
  /**
   * This method should not follow redirects, it simply gathers a summary of the immediate HTTP response with the
   * relevant information, ie the status code and the location header if present.
   */
  def fetchResponseFor(uri: URI)(implicit ec: ExecutionContext): Future[HttpResponseSummary]
}

object UrlResponseFetcher {
  case class HttpResponseSummary(statusCode: Int, maybeLocationHeader: Option[HttpResponseSummary.LocationHeader]) {
    def absoluteRedirectRelativeTo(requestUri: URI): Option[URI] = for {
      locationHeader <- maybeLocationHeader if HTTPRedirectStatusCodes.contains(statusCode)
    } yield locationHeader.asAbsoluteUriRelativeTo(requestUri)

    def asFollowResultGiven(requestUri: URI): FollowResult =
      absoluteRedirectRelativeTo(requestUri).fold[FollowResult](Conclusion(Success(statusCode)))(Redirect(_))
  }

  object HttpResponseSummary {
    val HTTPRedirectStatusCodes: Set[Int] = Set(301, 302, 303, 307, 308)

    /**
     * Raw value of the location header, which may be a path or a full URL.
     */
    case class LocationHeader(value: String) {
      def asAbsoluteUriRelativeTo(baseUri: URI): URI = baseUri.resolve(value)
    }
  }

  val JavaNetHttpResponseFetcher = new UrlResponseFetcher {
    val client = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
      .build()

    private def getHeadResponse(uri: URI): Future[HttpResponse[Void]] = {
      val request = HttpRequest.newBuilder()
        .uri(uri)
        .timeout(Duration.ofMinutes(2))
        .header("Content-Type", "application/json")
        .method("HEAD", BodyPublishers.noBody())
        .build()

      client.sendAsync(request, BodyHandlers.discarding()).asScala
    }

    override def fetchResponseFor(uri: URI)(implicit ec: ExecutionContext): Future[HttpResponseSummary] = for {
      response <- getHeadResponse(uri)
    } yield HttpResponseSummary(response.statusCode, response.headers().firstValue("Location").toScala.map(LocationHeader.apply))
  }
}

