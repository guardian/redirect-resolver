import sbtrelease.ReleaseStateTransformations._
import sbtversionpolicy.withsbtrelease.ReleaseVersion

ThisBuild / scalaVersion := "2.13.17"

libraryDependencies ++= Seq(
  "com.github.blemale" %% "scaffeine" % "5.3.0",
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "org.http4s" %% "http4s-blaze-server" % "0.23.17" % Test,
  "org.http4s" %% "http4s-dsl" % "0.23.32" % Test,
  "ch.qos.logback" % "logback-classic" % "1.5.20" % Test
)

lazy val root = (project in file("."))
  .settings(
    name := "redirect-resolver",
    organization := "com.gu",
    crossScalaVersions := Seq("3.3.6", scalaVersion.value),
    Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
  )

licenses := Seq(License.Apache2)
releaseVersion := ReleaseVersion.fromAssessedCompatibilityWithLatestRelease().value
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
