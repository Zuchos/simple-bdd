import Dependencies._

ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.zuchos"
ThisBuild / organizationName := "zuchos"

val akkaVersion = "2.5.20"
val sttpVersion = "1.5.0"

lazy val otherDependencies = Seq(
  "com.softwaremill.common" %% "tagging" % "2.2.1",
)

lazy val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.5",
  "org.mockito" % "mockito-core" % "2.23.4",
  "com.softwaremill.quicklens" %% "quicklens" % "1.4.11",
  "org.scalacheck" %% "scalacheck" % "1.14.0",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "de.heikoseeberger" %% "akka-http-circe" % "1.25.2",
  "org.clapper" %% "classutil" % "1.4.0",
  "com.tngtech.archunit" % "archunit-junit" % "0.8.3",
  "com.github.chocpanda" %% "scalacheck-magnolia" % "0.2.2",
  "io.chrisdavenport" %% "cats-scalacheck" % "0.1.0",
  "org.java-websocket" % "Java-WebSocket" % "1.4.0"
).map(_ % Test)

lazy val root = (project in file("."))
  .settings(
    name := "simple-bdd",
    libraryDependencies ++= testDependencies ++ otherDependencies
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
