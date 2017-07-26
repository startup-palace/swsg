package swsg

import better.files._

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
}
