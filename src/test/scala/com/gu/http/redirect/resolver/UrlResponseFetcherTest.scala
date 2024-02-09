package com.gu.http.redirect.resolver

import com.gu.http.redirect.resolver.UrlResponseFetcher.HttpResponseSummary.LocationHeader
import com.gu.http.redirect.resolver.UrlResponseFetcher.javaNetHttpFollower
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI
import scala.concurrent.ExecutionContext.Implicits.global

class UrlResponseFetcherTest extends AnyFlatSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues {
  it should "resolve a BBC url" in {
    whenReady(
      javaNetHttpFollower.fetchResponseFor(URI.create("https://www.bbc.co.uk/news/uk-politics-63534039"))
    ){
      maybeUri => maybeUri.maybeLocationHeader.value shouldBe LocationHeader("/news/av/uk-politics-63534039")
    }
  }
}
