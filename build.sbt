name := "SWSG"

scalaVersion := "2.12.3"

version := "0.1.0-SNAPSHOT"

lazy val root = project.in(file(".")).aggregate(js, jvm)

lazy val swsg = crossProject
  .in(file("."))
  .settings(
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Xlint:-missing-interpolator"
    ),
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest"  % "3.0.4" % Test,
      "org.parboiled" %%% "parboiled" % "2.1.4"
    ),
    scalafmtVersion in ThisBuild := "1.2.0",
    scalafmtOnCompile in ThisBuild := true,
    scalafmtTestOnCompile in ThisBuild := true,
    ignoreErrors in (ThisBuild, scalafmt) := false,
    TwirlKeys.templateFormats += ("php" -> "swsg.backend.PhpFormat"),
    TwirlKeys.templateImports := Seq(
      "play.twirl.api.TwirlFeatureImports._",
      //"play.twirl.api.TwirlHelperImports._",
      "play.twirl.api.Txt",
      //"swsg._",
      "swsg.backend._"
    )
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.github.pathikrit" %% "better-files" % "3.1.0",
      "com.github.scopt"     %% "scopt"        % "3.7.0"
    ),
    assemblyJarName in assembly := "swsg.jar",
    test in assembly := {}
  )
  .jsSettings(
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(SbtTwirl)

lazy val jvm = swsg.jvm
lazy val js  = swsg.js
