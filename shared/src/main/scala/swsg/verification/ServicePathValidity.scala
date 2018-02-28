package swsg.verification

import swsg._
import swsg.Model._

final case object ServicePathValidity extends Verification {
  def run(model: Model): Seq[ModelError] = {
    model.services.flatMap(checkService)
  }

  private def checkService(s: Service): Seq[ModelError] = {
    val actualParameters = parseParameters(s.path).toVector
    val expectedParameters = s.params.toVector
      .filter(_.location == Path)
      .map(_.variable.name)

    val missing = expectedParameters
      .diff(actualParameters)
      .map(name => MissingParameterInPathError(s.name, name))
    val unknown = actualParameters
      .diff(expectedParameters)
      .map(name => UnknownParameterInPathError(s.name, name))

    missing ++ unknown
  }

  private def parseParameters(path: String): Seq[Identifier] = {
    val regex = """\{([A-Za-z0-9_]+)\}""".r
    regex.findAllMatchIn(path).map(_.group(1)).toSeq
  }
}
