package swsg.verification

import swsg._
import swsg.Model._

final case object ContextVariablesTypeValidity extends Verification {
  def run(model: Model): Seq[ModelError] = {
    val acErrors = model.atomicComponents.toSeq
      .flatMap { ac =>
        val check = checkVariables(ContextElement.AtomicComponent, ac.name)(_)
        check(ac.pre) ++ check(ac.add) ++ check(ac.rem)
      }

    val serviceErrors = model.services.flatMap(s =>
      checkVariables(ContextElement.Service, s.name)(s.params))

    acErrors ++ serviceErrors
  }

  private def checkVariables(
      elementType: ContextElement,
      elementName: Identifier)(variables: Set[Variable]): Seq[ModelError] = {
    variables.toSeq.flatMap {
      case Variable(n, Inherited) =>
        Seq(InheritedTypeInContext(elementType, elementName, n))
      case _ => Seq.empty
    }
  }
}
