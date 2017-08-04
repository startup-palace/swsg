package swsg.verification

import swsg._

final case object CompositeComponentNonEmptiness extends Verification {
  def run(model: Model): Seq[EmptyCompositeComponentError] = {
    model.compositeComponents.toSeq.flatMap { c =>
      if (c.components.isEmpty) {
        Seq(EmptyCompositeComponentError(c.name))
      } else {
        Seq.empty
      }
    }
  }
}
