package swsg

import better.files._
import swsg.backend._

final case object Tasks {
  def check(modelFile: String): Either[String, Model] = {
    val serializedModel: String = File(modelFile).contentAsString

    ModelParser.parse(serializedModel) match {
      case Left(e) => Left(e)
      case Right(model) => {
        ConsistencyVerification.run(model) match {
          case Seq() => {
            println("Model OK")
            Right(model)
          }
          case Seq(e) => Left(e.toString)
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
      s"'${path}' written"
    }

    for {
      backend <- backends.find(_.name == backendName).toRight(unknownBackend)
      model   <- check(modelFile)
      impl    <- Implementation.fromPath(implementationPath).left.map(_.toString)
    } yield backend.generate(model, impl).toSeq.map(writeOutput).mkString("\n")
  }
}
