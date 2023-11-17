package com.gu.http.redirect.resolver

import java.net.URI
import java.net.http.HttpClient.{Redirect, Version}
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.FutureConverters._
import scala.jdk.OptionConverters._

object UrlFollower {
  val javaNetHttpFollower = new UrlFollower {
    val client = HttpClient.newBuilder()
      .version(Version.HTTP_1_1)
      .followRedirects(Redirect.NEVER)
      .build()

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

    override def followOnce(uri: URI): Future[Option[LocationHeader]] = {
      for {
        response <- getHeadResponse(uri)
      } yield {
        response.headers().firstValue("Location").toScala.map(LocationHeader)
      }
    }
  }
}

/**
 * Raw value of the location header, which may be a path or a full URL.
 */
case class LocationHeader(value: String)

trait UrlFollower {
  /**
   * This method should not follow redirects, it simply evaluates if a URL does redirect and if it does where it
   * redirects to.
   */
  def followOnce(uri: URI): Future[Option[LocationHeader]]
}