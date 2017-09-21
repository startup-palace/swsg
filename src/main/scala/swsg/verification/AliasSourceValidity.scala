package swsg.verification

import swsg._
import swsg.Model._

final case object AliasSourceValidity extends Verification {
  def run(model: Model): Seq[AliasSourceValidityError] = {
    val ccErrors = model.compositeComponents.toSeq
      .flatMap(
        cc =>
          cc.components.flatMap(
            checkComponentInstance(Reference.CompositeComponent,
                                   cc.name,
                                   model.components)))
    val serviceErrors = model.services.flatMap(
      s =>
        checkComponentInstance(Reference.Service, s.name, model.components)(
          s.component))

    ccErrors ++ serviceErrors
  }

  private def checkComponentInstance(parentType: Reference.Source,
                                     parentName: Identifier,
                                     components: Set[Component])(
      ci: ComponentInstance): Seq[AliasSourceValidityError] = {
    val component = Reference.resolve(ci.component, components).get
    component match {
      case AtomicComponent(_, _, pre, add, rem) => {
        val variables     = pre ++ add ++ rem
        val variableNames = variables.map(_.name)
        val sources       = ci.aliases.map(_.source)
        val diff          = sources.diff(variableNames)
        diff.toSeq.map(name =>
          AliasSourceValidityError(parentType, parentName, name))
      }
      case CompositeComponent(_, _, _) => Seq.empty
    }
  }
}
