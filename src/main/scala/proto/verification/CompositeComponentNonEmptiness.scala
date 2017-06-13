package proto.verification

import proto._
import proto.Model._

final case object CompositeComponentNonEmptiness extends Verification {
  def run(model: Model): Seq[EmptyCompositeComponentError] = {
    val compositeComponents = model.components.collect {
      case c @ CompositeComponent(_, _, _) => c
    }
    compositeComponents.toSeq.flatMap { c =>
      if (c.components.isEmpty) {
        Seq(EmptyCompositeComponentError(c.name))
      } else {
        Seq.empty
      }
    }
  }
}
