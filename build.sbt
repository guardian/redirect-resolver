import sbtrelease.ReleaseStateTransformations._
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

ThisBuild / scalaVersion := "2.13.16"

resolvers ++= Resolver.sonatypeOssRepos("public")

libraryDependencies ++= List(
  "com.github.blemale" %% "scaffeine" % "5.3.0",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.http4s" %% "http4s-blaze-server" % "0.23.17" % Test,
  "org.http4s" %% "http4s-dsl" % "0.23.30" % Test,
  "ch.qos.logback" % "logback-classic" % "1.5.18" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "redirect-resolver",
    organization := "com.gu",
    crossScalaVersions := Seq("3.3.6", scalaVersion.value),
    Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
  )

licenses := Seq("Apache V2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))
releaseVersion := fromAggregatedAssessedCompatibilityWithLatestRelease().value
releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  setNextVersion,
  commitNextVersion
)
