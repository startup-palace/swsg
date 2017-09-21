package swsg.verification

import swsg._
import swsg.Model._

final case object AliasTargetValidity extends Verification {
  def run(model: Model): Seq[AliasTargetValidityError] = {
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
      ci: ComponentInstance): Seq[AliasTargetValidityError] = {
    val component = Reference.resolve(ci.component, components).get
    component match {
      case AtomicComponent(_, _, _, add, _) => {
        val addNames     = add.map(_.name)
        val targets      = ci.aliases.map(_.target)
        val intersection = targets.intersect(addNames)
        intersection.toSeq.map(name =>
          AliasTargetValidityError(parentType, parentName, name))
      }
      case CompositeComponent(_, _, _) => Seq.empty
    }
  }
}
