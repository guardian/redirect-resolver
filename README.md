# redirect-resolver
_given a url, where does that url ultimately redirect to?_

[![redirect-resolver Scala version support](https://index.scala-lang.org/guardian/redirect-resolver/redirect-resolver/latest-by-scala-version.svg?platform=jvm)](https://index.scala-lang.org/guardian/redirect-resolver/redirect-resolver)
[![Release](https://github.com/guardian/redirect-resolver/actions/workflows/release.yml/badge.svg)](https://github.com/guardian/redirect-resolver/actions/workflows/release.yml)

* Set a maximum number of redirects to follow
* Redirect-loop detection
* Caching at the individual redirect level - doesn't need to follow the same redirect twice, even if the original redirect path started in a different place

