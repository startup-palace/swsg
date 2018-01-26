package swsg

import better.files._
import swsg.backend._

final case object Tasks {
  def readModel(modelFile: String): Either[String, Model] = {
    import io.circe.parser.{parse => parseJson}

    val serializedModel: String = File(modelFile).contentAsString

    val parsedModel = parseJson(serializedModel) match {
      case Right(_) =>
        val model = OpenApiConverter
          .fromJson(serializedModel)
          .left
          .map(_.map(_.toString))
          .flatMap(OpenApiConverter.toModel)
          .left
          .map(_.mkString("\n"))
        ("OpenAPI Json", model)
      case Left(_) => ("Custom syntax", ModelParser.parse(serializedModel))
    }

    println(s"Model format: ${parsedModel._1}")
    parsedModel._2
  }

  def check(modelFile: String): Either[String, Model] = {
    readModel(modelFile) match {
      case Left(e) => Left(e)
      case Right(model) => {
        ConsistencyVerification.run(model).toVector match {
          case Vector() => {
            println("Model OK")
            Right(model)
          }
          case s @ Vector(_*) => Left(s.mkString("\n"))
        }
      }
    }
  }

  def generate(modelFile: String,
               implementationPath: String,
               backendName: String,
               outputPath: String): Either[String, String] = {
    val backends = Set(Laravel)
    lazy val unknownBackend =
      s"""Backend '${backendName}' is not one of the supported backends (${backends
        .map(_.name)
        .mkString(", ")})"""
    def writeOutput(item: (String, String)): String = {
      val path = (outputPath / item._1)
      path
        .createIfNotExists(createParents = true)
        .write(item._2)
      s"Wrote '${path}'"
    }
    val postMsg = """

To enable generated code, edit "config/app.php" and replace
  App\Providers\RouteServiceProvider::class
by
  App\Providers\GeneratedRouteServiceProvider::class
"""

    val output = for {
      backend <- backends.find(_.name == backendName).toRight(unknownBackend)
      model   <- check(modelFile)
      impl <- Implementation
        .fromPath(model, implementationPath)
        .left
        .map(_.toString)
    } yield backend.generate(model, impl).toSeq.map(writeOutput).mkString("\n")

    output.map(_ ++ postMsg)
  }
}
