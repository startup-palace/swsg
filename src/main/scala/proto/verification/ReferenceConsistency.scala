package proto.verification

import proto._
import proto.Model._

final case object ReferenceConsistency extends AutoVerification {
  val levels = Seq(
    Set(
      ComponentRefConsistency,
      EntityRefConsistency
    ))
}

final case object ComponentRefConsistency extends Verification {
  def run(model: Model): Seq[BrokenReferenceError] = {
    val compositeComponents = model.components.collect {
      case c @ CompositeComponent(_, _, _) => c
    }
    val references: Seq[(Reference.Source, Identifier, ComponentRef)] =
      compositeComponents.toSeq.flatMap { cc =>
        cc.components.map(ci =>
          (Reference.CompositeComponent, cc.name, ci.component))
      } ++ model.services.toSeq.flatMap { s =>
        Seq((Reference.Service, s.name, s.component.component))
      }
    val checkedReferences
      : Seq[((Reference.Source, Identifier, ComponentRef), Boolean)] =
      references.map { r =>
        (r, Reference.resolve(r._3, model.components).isDefined)
      }
    checkedReferences.filter(!_._2).map(_._1).map { cr =>
      BrokenReferenceError(cr._1, cr._2, Reference.Component, cr._3.target)
    }
  }
}

final case object EntityRefConsistency extends Verification {
  def run(model: Model): Seq[BrokenReferenceError] = {
    val references: Seq[(Reference.Source, Identifier, EntityRef)] =
      model.entities.toSeq.flatMap { e =>
        e.attributes.collect {
          case Variable(_, r @ EntityRef(_)) => (Reference.Entity, e.name, r)
        }
      }
    val checkedReferences
      : Seq[((Reference.Source, Identifier, EntityRef), Boolean)] =
      references.map { r =>
        (r, Reference.resolve(r._3, model.entities).isDefined)
      }
    checkedReferences.filter(!_._2).map(_._1).map { cr =>
      BrokenReferenceError(cr._1, cr._2, Reference.Entity, cr._3.target)
    }
  }
}
