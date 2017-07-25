package wssg.verification

import wssg._
import wssg.Model._

final case object ComponentInstanceParametersExhaustivity extends Verification {
  def run(model: Model): Seq[ModelError] = {
    val compositeComponents = model.components.collect {
      case c @ CompositeComponent(_, _, _) => c
    }

    val ccErrors = compositeComponents.toSeq
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

  private def checkComponentInstance(
      parentType: Reference.Source,
      parentName: Identifier,
      parentScope: Set[Variable],
      components: Set[Component])(ci: ComponentInstance): Seq[ModelError] = {
    val component  = Reference.resolve(ci.component, components).get
    val parameters = component.params.toSeq
    val arguments  = ci.bindings.toSeq.map(_.param)
    val bindings   = ci.bindings.toSeq

    val missing: Seq[MissingArgumentError] = parameters
      .diff(arguments)
      .map(p => MissingArgumentError(parentType, parentName, component.name, p))
    val useless: Seq[UselessArgumentError] = arguments
      .diff(parameters)
      .map(p => UselessArgumentError(parentType, parentName, component.name, p))
    val notInScope: Seq[NotInScopeArgumentError] = bindings
      .flatMap {
        case Binding(a @ Variable(_, _), v @ Variable(_, _)) => {
          if (parentScope.contains(v)) {
            Seq.empty
          } else {
            Seq(
              NotInScopeArgumentError(parentType,
                                      parentName,
                                      component.name,
                                      a,
                                      v))
          }
        }
        case Binding(_, _) => Seq.empty
      }

    missing ++ useless ++ notInScope
  }
}
