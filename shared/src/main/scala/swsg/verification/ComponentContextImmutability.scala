package swsg.verification

import swsg._

final case object ComponentContextImmutability extends Verification {
  def run(model: Model): Seq[VariableOverrideError] = {
    model.atomicComponents.toSeq.flatMap { c =>
      c.add.toSeq.flatMap { v =>
        val alreadyInContext = c.pre.find(_.name == v.name)
        if (alreadyInContext.isDefined) {
          Seq(VariableOverrideError(c.name, v.name))
        } else {
          Seq.empty
        }
      }
    }
  }
}
