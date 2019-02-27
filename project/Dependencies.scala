import sbt._

object Dependencies {
  lazy val betterFiles = "com.github.pathikrit" %% "better-files" %  "3.7.0"

  // -- Logging
  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  lazy val slf4jBackend = "org.slf4j" % "slf4j-simple" % "1.7.26"

  // -- Testing
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
}
