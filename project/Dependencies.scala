import sbt._

object Dependencies {
  lazy val scalaTest      = "org.scalatest" %% "scalatest" % "3.0.3"
  lazy val latestScalafmt = "1.1.0"
  lazy val parboiled      = "org.parboiled" %% "parboiled" % "2.1.4"
  lazy val betterFiles    = "com.github.pathikrit" %% "better-files" % "3.0.0"
  lazy val scopt          = "com.github.scopt" %% "scopt" % "3.6.0"
}
