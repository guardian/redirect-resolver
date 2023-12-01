package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

case class UltimateResponse(statusCode: Int, uri: URI)

class UrlResolver(urlFollower: UrlFollower) {

  val cache: AsyncLoadingCache[URI, Either[LocationHeader, Int]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture((i: URI) => urlFollower.followOnce(i))

  def resolve(uri: URI, count: Int = 10): Future[Either[String, UltimateResponse]] = {
    if (count == 0) {
      Future.successful(Left("too many redirects"))
    } else for {
      response <- cache.get(uri)
      result <- response.fold(locationHeader => resolve(uri.resolve(locationHeader.value), count - 1), statusCode => Future.successful(Right(UltimateResponse(statusCode, uri))))
    } yield result
  }
}
