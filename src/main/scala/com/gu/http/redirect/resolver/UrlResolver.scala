package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.gu.http.redirect.resolver.Resolution.{Resolved, Unresolved}
import com.gu.http.redirect.resolver.UrlResolver.{Conclusion, Redirect}

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}

sealed trait Resolution {
  val redirectPath: RedirectPath
}

object Resolution {
  case class Resolved(redirectPath: RedirectPath, statusCode: Try[Int]) extends Resolution

  case class Unresolved(redirectPath: RedirectPath) extends Resolution
}

object UrlResolver {
  type Conclusion = Try[Int]
  type Redirect = URI
}

class UrlResolver(fetcher: UrlResponseFetcher, maxRedirects: Int = 10) {

  val cache: AsyncLoadingCache[URI, Either[Redirect, Conclusion]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture(followOnce)

  private def followOnce(uri: URI): Future[Either[Redirect, Conclusion]] = {
    fetcher.fetchResponseFor(uri).transform {
      case Success(summary) =>
        Success(summary.absoluteRedirectRelativeTo(uri).toLeft(Success(summary.statusCode)))
      case Failure(exception) =>
        Success(Right(Failure(exception)))
    }
  }

  def resolve(uri: URI): Future[Resolution] = resolveFollowing(RedirectPath(uri))

  private def resolveFollowing(redirectPath: RedirectPath): Future[Resolution] =
    if (redirectPath.numRedirects >= maxRedirects || redirectPath.isLoop) Future.successful(Unresolved(redirectPath))
    else cache.get(redirectPath.latestUri).flatMap { _.fold(
      subsequentUri => resolveFollowing(redirectPath.adding(subsequentUri)),
      tryFinalStatusCode => Future.successful(Resolved(redirectPath, tryFinalStatusCode))
    )}
}
