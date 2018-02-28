package swsg.verification

import swsg._
import swsg.Model._

final case object ServiceParameterNameUnicity extends Verification {
  def run(model: Model): Seq[ServiceParameterNameUnicityError] = {
    model.services.flatMap(checkService)
  }

  private def checkService(
      s: Service): Seq[ServiceParameterNameUnicityError] = {
    val sources       = s.params.toVector.map(_.variable.name)
    val uniqueSources = s.params.map(_.variable.name).toVector
    val diff          = sources.diff(uniqueSources)
    val duplicates    = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq
      .map {
        case (source, occurences) =>
          (s.name, source, occurences)
      }
      .map(ServiceParameterNameUnicityError.tupled)
  }
}
