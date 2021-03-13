import Dependencies._

ThisBuild / scalaVersion     := "2.13.4"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "net.wasamon"
ThisBuild / organizationName := "WasaLabo, LLC."

lazy val root = (project in file("."))
  .settings(
    name := "ptp",
    libraryDependencies += scalaTest % Test,
    resolvers += "Github Repository" at "https://synthesijer.github.io/web/pub/",
    libraryDependencies += "synthesijer" % "synthesijer" % "3.1.0"
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
