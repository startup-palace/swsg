import Dependencies._

lazy val root = (project in file(".")).settings(
  inThisBuild(
    List(
      scalaVersion := "2.12.2",
      version := "0.1.0-SNAPSHOT"
    )),
  name := "Proto",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-Xfatal-warnings",
    "-Xfuture",
    "-Xlint"
  ),
  libraryDependencies ++= Seq(
    scalaTest % Test,
    parboiled,
    betterFiles,
    scopt
  ),
  scalafmtVersion in ThisBuild := latestScalafmt,
  scalafmtOnCompile in ThisBuild := true,
  scalafmtTestOnCompile in ThisBuild := true,
  ignoreErrors in (ThisBuild, scalafmt) := false,
  assemblyJarName in assembly := "wssg.jar",
  test in assembly := {}
)
