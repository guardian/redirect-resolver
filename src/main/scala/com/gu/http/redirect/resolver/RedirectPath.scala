package com.gu.http.redirect.resolver

import java.net.URI

case class RedirectPath(locations: Seq[URI]) {
  val originalUri: URI = locations.head
  val latestUri: URI = locations.last
  val numRedirects: Int = locations.size - 1
  val doesRedirect: Boolean = numRedirects > 0

  val isLoop: Boolean = locations.dropRight(1).contains(locations.last)

  def adding(location: URI): RedirectPath = copy(locations :+ location)
}

object RedirectPath {
  def apply(uri: URI): RedirectPath = RedirectPath(Seq(uri))
}
