package com.gu.http.redirect.resolver

import java.net.URI
import scala.util.{Success, Try}

/**
 * The result of following a url once - could either be a redirect, or some kind of conclusion.
 */
sealed trait FollowResult

case class Redirect(uri: URI) extends FollowResult

/**
 * Could be DNS or Network failure, or a successful HTTP status code.
 */
case class Conclusion(statusCode: Try[Int]) extends FollowResult {

  val isOk: Boolean = statusCode.toOption.contains(200)
}

object Conclusion {
  val Ok: Conclusion = Conclusion(Success(200))
}

