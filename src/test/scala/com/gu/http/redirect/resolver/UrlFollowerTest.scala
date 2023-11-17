package com.gu.http.redirect.resolver

import com.gu.http.redirect.resolver.UrlFollower.javaNetHttpFollower
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI

class UrlFollowerTest extends AnyFlatSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues {
  it should "resolve a BBC url" in {
    whenReady(
      javaNetHttpFollower.followOnce(URI.create("https://www.bbc.co.uk/news/uk-politics-63534039"))
    ){
      maybeUri => maybeUri.value shouldBe LocationHeader("/news/av/uk-politics-63534039")
    }
  }
}
