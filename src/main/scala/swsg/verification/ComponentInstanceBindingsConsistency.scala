package swsg.verification

import swsg._
import swsg.Model._

final case object ComponentInstanceBindingsConsistency extends Verification {
  def run(model: Model): Seq[IncorrectBindingError] = {
    val ccErrors = model.compositeComponents.toSeq
      .flatMap(
        cc =>
          cc.components.flatMap(
            checkComponentInstance(Reference.CompositeComponent,
                                   cc.name,
                                   cc.params,
                                   model.components)))
    val serviceErrors = model.services.flatMap(
      s =>
        checkComponentInstance(Reference.Service,
                               s.name,
                               Set.empty,
                               model.components)(s.component))

    ccErrors ++ serviceErrors
  }

  private def checkComponentInstance(parentType: Reference.Source,
                                     parentName: Identifier,
                                     parentScope: Set[Variable],
                                     components: Set[Component])(
      ci: ComponentInstance): Seq[IncorrectBindingError] = {
    val component = Reference.resolve(ci.component, components).get
    val bindings  = ci.bindings.toSeq

    def error(expected: Variable, found: Type): IncorrectBindingError =
      IncorrectBindingError(parentType,
                            parentName,
                            component.name,
                            expected,
                            found)

    bindings.flatMap {
      case Binding(Variable(_, t1), Constant(t2, _)) if t1 == t2 => Seq.empty
      case Binding(Variable(_, t1), Variable(_, t2)) if t1 == t2 => Seq.empty
      case Binding(expected @ Variable(_, _), Constant(found, _)) =>
        Seq(error(expected, found))
      case Binding(expected @ Variable(_, _), Variable(_, found)) =>
        Seq(error(expected, found))
    }
  }
}
