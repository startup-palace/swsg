package swsg

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {
  @JSExportTopLevel("swsg.parse")
  def parse(input: String): Model = {
    import io.circe.parser.{parse => parseJson}

    val parsedModel = parseJson(input) match {
      case Right(_) =>
        val model = OpenApiConverter
          .fromJson(input)
          .left
          .map(_.map(_.toString))
          .flatMap(OpenApiConverter.toModel)
          .left
          .map(_.mkString("\n"))
        ("OpenAPI Json", model)
      case Left(_) => ("Custom syntax", ModelParser.parse(input))
    }

    parsedModel._2 match {
      case Left(e)  => throw new RuntimeException(e)
      case Right(m) => m
    }
  }

  @JSExportTopLevel("swsg.modelToJson")
  def modelToJson(model: Model): String = {
    import io.circe.syntax._
    import ModelInstances.encodeModel

    model.asJson.spaces2
  }

  @JSExportTopLevel("swsg.check")
  def check(model: Model): String = {
    import io.circe.syntax._
    import ModelErrorInstances.encodeModelError

    ConsistencyVerification.run(model).toList.asJson.spaces2
  }
}
