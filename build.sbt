ThisBuild / version := "0.1.0-SNAPSHOT"

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
    name := "scala-school-redirects",
    Test / testOptions +=
      Tests.Argument(TestFrameworks.ScalaTest, "-u", s"test-results/scala-${scalaVersion.value}", "-o")
  )

