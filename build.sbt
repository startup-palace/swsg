name := "SWSG"

lazy val scalaVer = "2.12.4"

scalaVersion := scalaVer

version := "0.1.0-SNAPSHOT"

lazy val circeVersion = "0.9.1"

lazy val catsVersion = "1.0.1"

lazy val root = project.in(file(".")).aggregate(js, jvm)

lazy val swsg = crossProject
  .in(file("."))
  .settings(
    scalaVersion := scalaVer,
    scalacOptions ++= Seq(
      "-unchecked",
      "-deprecation",
      "-feature",
      "-Xfatal-warnings",
      "-Xfuture",
      "-Xlint",
      "-Xlint:-missing-interpolator",
      "-Ypartial-unification"
    ),
    scalacOptions in (Compile, console) ~= (_ filterNot (_ == "-Xfatal-warnings")),
    scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % "3.0.4" % Test,
      "com.chuusai"   %%% "shapeless" % "2.3.2",
      "org.parboiled" %%% "parboiled" % "2.1.4"
    ),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-generic-extras",
      "io.circe" %%% "circe-parser"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core",
      "org.typelevel" %%% "cats-kernel",
      "org.typelevel" %%% "cats-macros"
    ).map(_ % catsVersion),
    scalafmtVersion in ThisBuild := "1.4.0",
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
      "com.github.pathikrit" %% "better-files"  % "3.4.0",
      "com.github.scopt"     %% "scopt"         % "3.7.0",
      "org.scala-js"         %% "scalajs-stubs" % scalaJSVersion % "provided"
    ),
    assemblyJarName in assembly := "swsg.jar",
    test in assembly := {}
  )
  .jsSettings()
  .enablePlugins(SbtTwirl)

lazy val jvm = swsg.jvm
lazy val js  = swsg.js
