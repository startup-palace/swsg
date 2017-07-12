package proto

import better.files._

final case object Tasks {
  def check(modelFile: String): Either[String, Unit] = {
    val serializedModel: String = File(modelFile).contentAsString

    ModelParser.parse(serializedModel) match {
      case Left(e) => Left(e)
      case Right(model) => {
        ConsistencyVerification.run(model) match {
          case Seq() => {
            println("Model OK")
            Right(())
          }
          case Seq(e) => Left(e.toString)
        }
      }
    }
  }
}
