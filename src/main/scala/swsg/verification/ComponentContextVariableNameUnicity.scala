package swsg.verification

import swsg._
import swsg.Model._

final case object ComponentContextVariableNameUnicity extends Verification {
  def run(model: Model): Seq[ComponentContextVariableNameUnicityError] = {
    model.atomicComponents.toSeq.flatMap(checkAtomicComponent)
  }

  private def checkAtomicComponent(component: AtomicComponent)
    : Seq[ComponentContextVariableNameUnicityError] = {
    val variables           = component.pre ++ component.add ++ component.rem
    val variableNames       = variables.toVector.map(_.name)
    val uniqueVariableNames = variables.map(_.name).toVector
    val diff                = variableNames.diff(uniqueVariableNames)
    val duplicates          = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq
      .map {
        case (variableName, occurences) =>
          (component.name, variableName, occurences)
      }
      .map(ComponentContextVariableNameUnicityError.tupled)
  }
}
