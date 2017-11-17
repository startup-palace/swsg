package swsg

final case object Main extends App {

  final case class RawCfg(
      mode: String = "check",
      modelFile: String = "./examples/registration/registration.model",
      implementationPath: String = "./examples/registration/impl/",
      backend: String = "laravel",
      outputPath: String = "./examples/registration/output/") {
    lazy val toConfig: Either[String, Config] = this.mode match {
      case "check" =>
        Right(CheckConfig(this.modelFile))
      case "gen" =>
        Right(
          GenConfig(this.modelFile,
                    this.implementationPath,
                    this.backend,
                    this.outputPath))
      case _ => Left(s"'${this.mode}' is not a supported mode")
    }
  }

  sealed abstract trait Config
  final case class CheckConfig(modelFile: String) extends Config
  final case class GenConfig(modelFile: String,
                             implementationPath: String,
                             backend: String,
                             outputPath: String)
      extends Config

  // Check if the given path is an existing file
  def isFile(parser: scopt.OptionParser[_])(x: String): Either[String, Unit] = {
    import better.files._
    val file = File(x)
    if (file.isRegularFile(File.LinkOptions.follow) && !file.isEmpty(
          File.LinkOptions.follow)) {
      parser.success
    } else {
      parser.failure(s"""File "$x" doesn't exists or is empty""")
    }
  }

  // Check if the given path is an existing directory
  def isDir(parser: scopt.OptionParser[_])(x: String): Either[String, Unit] = {
    import better.files._
    val file = File(x)
    if (file.isDirectory(File.LinkOptions.follow)) {
      parser.success
    } else {
      parser.failure(s"""Directory "$x" doesn't exists""")
    }
  }

  val parser = new scopt.OptionParser[RawCfg]("java -jar platform.jar") {

    help("help").text("prints this usage text")

    opt[String]('m', "model")
      .valueName("<file>")
      .validate(isFile(this))
      .action((x, c) => c.copy(modelFile = x))
      .text("path to the model")

    opt[String]('i', "implementation")
      .valueName("<path>")
      .validate(isDir(this))
      .action((x, c) => c.copy(implementationPath = x))
      .text("path to implementation")

    opt[String]('b', "backend")
      .valueName("<name>")
      .action((x, c) => c.copy(backend = x))
      .text("backend name")

    opt[String]('o', "output")
      .valueName("<path>")
      .action((x, c) => c.copy(outputPath = x))
      .text("path to the output directory")

    cmd("check")
      .text("Check a web app model")
      .action((_, c) => c.copy(mode = "check"))

    cmd("gen")
      .text("Generate from a web app model")
      .action((_, c) => c.copy(mode = "gen"))

  }

  parser.parse(args, RawCfg()) match {
    case Some(cfg) =>
      cfg.toConfig match {
        case Right(config) => {
          println(config)

          val result = config match {
            case CheckConfig(modelFile) => Tasks.check(modelFile)
            case GenConfig(modelFile,
                           implementationPath,
                           backend,
                           outputPath) =>
              Tasks.generate(modelFile, implementationPath, backend, outputPath)
          }

          result match {
            case Left(error) => {
              println(error)
              sys.exit(1)
            }
            case Right(output) => println(output)
          }

        }
        case Left(error) => {
          println(error)
          sys.exit(1)
        }
      }
    case None => sys.exit(1)
  }

}
