package com.gu.http.redirect.resolver

import com.gu.http.redirect.resolver.Resolution.Resolved
import com.gu.http.redirect.resolver.UrlResponseFetcher.JavaNetHttpResponseFetcher
import org.scalatest.EitherValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.net.URI
import scala.util.Success

class UrlResolverTest extends AnyFlatSpec with Matchers with ScalaFutures with IntegrationPatience with EitherValues {
  it should "resolve a BBC url" in {
    val urlResolver = new UrlResolver(JavaNetHttpResponseFetcher)
    whenReady(
      urlResolver.resolve(URI.create("https://www.bbc.co.uk/news/uk-politics-63534039"))
    ){
      resolution => resolution shouldBe
        Resolved(RedirectPath(Seq(URI.create("https://www.bbc.co.uk/news/uk-politics-63534039"),
          URI.create("https://www.bbc.co.uk/news/av/uk-politics-63534039"))), Conclusion(Success(200)))
    }
  }

  it should "not crash for domains that do not exist" in {
    val uriWithNonExistentDomain = URI.create("https://www.doesnotexist12312312234523532534546.com/")
    val urlResolver = new UrlResolver(JavaNetHttpResponseFetcher)

    whenReady(
      urlResolver.resolve(uriWithNonExistentDomain)
    ){
      resolution =>
        resolution.redirectPath shouldBe RedirectPath(Seq(uriWithNonExistentDomain))
    }
  }
}
