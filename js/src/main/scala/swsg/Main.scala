package swsg

import scala.scalajs.js.annotation.JSExportTopLevel

object Main {
  @JSExportTopLevel("swsg.parse")
  def parse(input: String): Model = ModelParser.parse(input) match {
    case Left(e)  => throw new RuntimeException(e)
    case Right(m) => m
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
