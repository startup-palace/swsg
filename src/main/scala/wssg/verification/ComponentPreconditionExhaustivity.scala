package wssg.verification

import wssg._
import wssg.Model._

final case object ComponentPreconditionExhaustivity extends Verification {
  def run(model: Model): Seq[VariableMissingInPreconditionsError] = {
    val atomicComponents = model.components.collect {
      case c @ AtomicComponent(_, _, _, _, _) => c
    }
    atomicComponents.toSeq.flatMap { c =>
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
