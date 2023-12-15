import sbtrelease.ReleaseStateTransformations._
import sbtversionpolicy.withsbtrelease.ReleaseVersion.fromAggregatedAssessedCompatibilityWithLatestRelease

ThisBuild / scalaVersion := "2.13.12"

val Http4sVersion = "0.23.14"

libraryDependencies ++= List(
  "com.github.blemale" %% "scaffeine" % "5.2.1",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion % Test,
  "org.http4s" %% "http4s-dsl" % Http4sVersion % Test,
  "ch.qos.logback" % "logback-classic" % "1.4.7" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "redirect-resolver",
    organization := "com.gu",
    Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
  )

licenses := Seq("Apache V2" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))
releaseVersion := fromAggregatedAssessedCompatibilityWithLatestRelease().value
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
