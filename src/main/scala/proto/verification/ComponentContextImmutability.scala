package proto.verification

import proto._
import proto.Model._

final case object ComponentContextImmutability extends Verification {
  def run(model: Model): Seq[VariableOverrideError] = {
    val atomicComponents = model.components.collect {
      case c @ AtomicComponent(_, _, _, _, _) => c
    }
    atomicComponents.toSeq.flatMap { c =>
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
