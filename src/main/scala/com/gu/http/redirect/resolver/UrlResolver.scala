package com.gu.http.redirect.resolver

import com.github.blemale.scaffeine.{AsyncLoadingCache, Scaffeine}
import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class UrlResolver(urlFollower: UrlFollower) {

  val cache: AsyncLoadingCache[URI, Option[LocationHeader]] =
    Scaffeine()
      .recordStats()
      .expireAfterWrite(10.minutes)
      .maximumSize(500)
      .buildAsyncFuture((i: URI) => urlFollower.followOnce(i))

  def resolve(uri: URI, count: Int = 10): Future[Either[String, URI]] = {
    println(s"resolve $uri, with count $count")
    if (count == 0) {
      Future.successful(Left("too many redirects"))
    } else {
      (for {
        response <- cache.get(uri)
      } yield response.fold(Future.successful[Either[String, URI]](Right(uri))) { location =>
        resolve(uri.resolve(location.value), count - 1)
        }).flatten
    }
  }

  println(Await.result(resolve(URI.create("http://localhost:8000/redirect-a")), 10.seconds))
  println(Await.result(resolve(URI.create("http://localhost:8000/redirect-b")), 10.seconds))
}
