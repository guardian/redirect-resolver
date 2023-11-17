ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val Http4sVersion = "0.22.0"
libraryDependencies ++= List(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.github.blemale" %% "scaffeine" % "5.2.1",
  "org.scalatest" %% "scalatest" % "3.2.17" % Test,
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-school-redirects"
  )

