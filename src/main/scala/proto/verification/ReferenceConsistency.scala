package proto.verification

import proto._
import proto.Model._

final case object ReferenceConsistency extends AutoVerification {
  val levels = Seq(
    Set(
      ComponentRefConsistency
    ))
}

final case object ComponentRefConsistency extends Verification {
  def run(model: Model): Seq[BrokenReferenceError] = {
    val compositeComponents = model.components.collect {
      case c @ CompositeComponent(_, _, _) => c
    }
    val references: Seq[(Reference.Source, String, ComponentRef)] =
      compositeComponents.toSeq.flatMap { cc =>
        cc.components.map(ci =>
          (Reference.CompositeComponent, cc.name, ci.component))
      } ++ model.services.toSeq.flatMap { s =>
        Seq((Reference.Service, s.name, s.component.component))
      }
    val checkedReferences
      : Seq[((Reference.Source, String, ComponentRef), Boolean)] =
      references.map { r =>
        (r, Reference.resolve(r._3, model.components).isDefined)
      }
    checkedReferences.filter(!_._2).map(_._1).map { cr =>
      BrokenReferenceError(cr._1, cr._2, Reference.Component, cr._3.target)
    }
  }
}
