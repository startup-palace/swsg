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
    scalaTest       % Test,
    "org.parboiled" %% "parboiled" % parboiled
  ),
  commands += Command.args("scalafmt", "Run scalafmt cli.") {
    case (state, args) =>
      val Right(scalafmt) =
        org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(latestScalafmt)
      scalafmt.main("--non-interactive" +: args.toArray)
      state
  }
)
