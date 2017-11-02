package swsg.verification

import swsg._
import swsg.Model._

final case object ReferenceConsistency extends AutoVerification {
  val levels = Seq(
    Set(
      ComponentRefConsistency,
      EntityRefConsistency
    ))
}

final case object ComponentRefConsistency extends Verification {
  def run(model: Model): Seq[BrokenReferenceError] = {
    val references: Seq[(Reference.Source, Identifier, ComponentRef)] =
      model.compositeComponents.toSeq.flatMap { cc =>
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
    val referencesFromE = model.entities.toSeq.flatMap(e =>
      toRefs(Reference.Entity, e.name, e.attributes))
    val referencesFromC = model.components.toSeq.flatMap {
      case AtomicComponent(n, p, pre, add, rem) =>
        Seq(
          toRefs(Reference.Component, n, p),
          toRefs(Reference.Component, n, pre),
          toRefs(Reference.Component, n, add),
          toRefs(Reference.Component, n, rem)
        ).flatten
      case CompositeComponent(n, p, _) => toRefs(Reference.Component, n, p)
    }
    val references = referencesFromE ++ referencesFromC
    val checkedReferences
      : Seq[((Reference.Source, Identifier, EntityRef), Boolean)] =
      references.map { r =>
        (r, Reference.resolve(r._3, model.entities).isDefined)
      }
    checkedReferences.filter(!_._2).map(_._1).map { cr =>
      BrokenReferenceError(cr._1, cr._2, Reference.Entity, cr._3.entity)
    }
  }

  private def toRefs(source: Reference.Source,
                     name: Identifier,
                     variables: Set[Variable])
    : Seq[(Reference.Source, Identifier, EntityRef)] = {
    variables.toSeq.collect {
      case Variable(_, r @ EntityRef(_)) => (source, name, r)
    }
  }
}
