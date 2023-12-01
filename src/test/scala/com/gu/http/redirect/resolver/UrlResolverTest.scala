package com.gu.http.redirect.resolver

import com.gu.http.redirect.resolver.UrlFollower.javaNetHttpFollower
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI

class UrlResolverTest extends AnyFlatSpec with Matchers with ScalaFutures with IntegrationPatience with EitherValues {
  it should "resolve a BBC url" in {
    val urlResolver = new UrlResolver(javaNetHttpFollower)
    whenReady(
      urlResolver.resolve(URI.create("https://www.bbc.co.uk/news/uk-politics-63534039"))
    ){
      ultimateResponse => ultimateResponse.value shouldBe UltimateResponse(200, URI.create("https://www.bbc.co.uk/news/av/uk-politics-63534039"))
    }
  }
}
