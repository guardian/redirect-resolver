package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import com.gu.http.redirect.resolver.Resolution.{Resolved, Unresolved}

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Failure

sealed trait Resolution {
  val redirectPath: RedirectPath
}

object Resolution {
  case class Resolved(redirectPath: RedirectPath, conclusion: Conclusion) extends Resolution

  case class Unresolved(redirectPath: RedirectPath) extends Resolution
}

class UrlResolver(fetcher: UrlResponseFetcher, maxRedirects: Int = 10) {

  val cache: AsyncLoadingCache[URI, FollowResult] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture(followOnce)

  private def followOnce(uri: URI): Future[FollowResult] =
    fetcher.fetchResponseFor(uri).map(_.asFollowResultGiven(uri)).recover {
      case exception => Conclusion(Failure(exception))
    }

  def resolve(uri: URI): Future[Resolution] = resolveFollowing(RedirectPath(uri))

  private def resolveFollowing(redirectPath: RedirectPath): Future[Resolution] =
    if (redirectPath.numRedirects >= maxRedirects || redirectPath.isLoop) Future.successful(Unresolved(redirectPath))
    else cache.get(redirectPath.latestUri).flatMap {
      case Redirect(subsequentUri) => resolveFollowing(redirectPath.adding(subsequentUri))
      case conclusion: Conclusion => Future.successful(Resolved(redirectPath, conclusion))
    }
}
