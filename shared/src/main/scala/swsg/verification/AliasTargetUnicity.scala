package swsg.verification

import swsg._
import swsg.Model._

final case object AliasTargetUnicity extends Verification {
  def run(model: Model): Seq[AliasTargetUnicityError] = {
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
      ci: ComponentInstance): Seq[AliasTargetUnicityError] = {
    val targets       = ci.aliases.toVector.map(_.target)
    val uniqueTargets = ci.aliases.map(_.target).toVector
    val diff          = targets.diff(uniqueTargets)
    val duplicates    = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq
      .map {
        case (target, occurences) =>
          (parentType, parentName, target, occurences)
      }
      .map(AliasTargetUnicityError.tupled)
  }
}
