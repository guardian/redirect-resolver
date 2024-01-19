package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.gu.http.redirect.resolver.Resolution.{Resolved, Unresolved}

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

sealed trait Resolution {
  val redirectPath: RedirectPath
}

object Resolution {
  case class Resolved(redirectPath: RedirectPath, statusCode: Int) extends Resolution

  case class Unresolved(redirectPath: RedirectPath) extends Resolution
}

class UrlResolver(urlFollower: UrlFollower, maxRedirects: Int = 10) {

  val cache: AsyncLoadingCache[URI, Either[LocationHeader, Int]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture((i: URI) => urlFollower.followOnce(i))

  def resolve(uri: URI): Future[Resolution] = resolve(RedirectPath(uri))

  private def resolve(redirectPath: RedirectPath): Future[Resolution] =
    if (redirectPath.numRedirects >= maxRedirects || redirectPath.isLoop) Future.successful(Unresolved(redirectPath))
    else {
      val uri = redirectPath.locations.last
      cache.get(uri).flatMap {
        resp =>
          resp.fold(
            locationHeader =>
              resolve(redirectPath.adding(locationHeader.asAbsoluteUriRelativeTo(uri))),
            ok => Future.successful(Resolved(redirectPath, ok))
          )
      }
    }
}
