package swsg.verification

import swsg._
import swsg.Model._

final case object AliasSourceUnicity extends Verification {
  def run(model: Model): Seq[AliasSourceUnicityError] = {
    val ccErrors = model.compositeComponents.toSeq
      .flatMap(
        cc =>
          cc.components.flatMap(
            checkComponentInstance(Reference.CompositeComponent, cc.name)))
    val serviceErrors = model.services.flatMap(s =>
      checkComponentInstance(Reference.Service, s.name)(s.component))

    ccErrors ++ serviceErrors
  }

  private def checkComponentInstance(parentType: Reference.Source,
                                     parentName: Identifier)(
      ci: ComponentInstance): Seq[AliasSourceUnicityError] = {
    val sources       = ci.aliases.toVector.map(_.source)
    val uniqueSources = ci.aliases.map(_.source).toVector
    val diff          = sources.diff(uniqueSources)
    val duplicates    = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq
      .map {
        case (source, occurences) =>
          (parentType, parentName, source, occurences)
      }
      .map(AliasSourceUnicityError.tupled)
  }
}
