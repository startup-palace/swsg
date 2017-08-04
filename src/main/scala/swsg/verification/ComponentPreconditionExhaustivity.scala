package swsg.verification

import swsg._

final case object ComponentPreconditionExhaustivity extends Verification {
  def run(model: Model): Seq[VariableMissingInPreconditionsError] = {
    model.atomicComponents.toSeq.flatMap { c =>
      c.rem.toSeq.flatMap { v =>
        val alreadyInContext = c.pre.find(_.name == v.name)
        if (alreadyInContext.isDefined) {
          Seq.empty
        } else {
          Seq(VariableMissingInPreconditionsError(c.name, v))
        }
      }
    }
  }
}
