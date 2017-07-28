import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(
      List(
        scalaVersion := "2.12.3",
        version := "0.1.0-SNAPSHOT"
      )),
    name := "WSSG",
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
    assemblyJarName in assembly := "swsg.jar",
    test in assembly := {},
    TwirlKeys.templateFormats += ("php" -> "swsg.backend.PhpFormat"),
    TwirlKeys.templateImports := Seq(
      "play.twirl.api.TwirlFeatureImports._",
      //"play.twirl.api.TwirlHelperImports._",
      "play.twirl.api.Txt",
      //"swsg._",
      "swsg.backend.Backend"
    )
  )
  .enablePlugins(SbtTwirl)
