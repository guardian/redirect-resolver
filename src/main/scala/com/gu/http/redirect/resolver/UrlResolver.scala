package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.gu.http.redirect.resolver.Resolution.{Resolved, Unresolved}

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

sealed trait Resolution {
  val redirectPath: RedirectPath
}

object Resolution {
  case class Resolved(redirectPath: RedirectPath, statusCode: Int) extends Resolution

  case class Unresolved(redirectPath: RedirectPath) extends Resolution
}

class UrlResolver(fetcher: UrlResponseFetcher, maxRedirects: Int = 10) {

  val cache: AsyncLoadingCache[URI, Either[URI, Int]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture(followOnce)

  private def followOnce(uri: URI): Future[Either[URI, Int]] =
    fetcher.fetchResponseFor(uri).map(summary => summary.absoluteRedirectRelativeTo(uri).toLeft(summary.statusCode))

  def resolve(uri: URI): Future[Resolution] = resolveFollowing(RedirectPath(uri))

  private def resolveFollowing(redirectPath: RedirectPath): Future[Resolution] =
    if (redirectPath.numRedirects >= maxRedirects || redirectPath.isLoop) Future.successful(Unresolved(redirectPath))
    else cache.get(redirectPath.latestUri).flatMap { _.fold(
      subsequentUri => resolveFollowing(redirectPath.adding(subsequentUri)),
      finalStatusCode => Future.successful(Resolved(redirectPath, finalStatusCode))
    )}
}
