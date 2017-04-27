package proto.verification

import proto._
import proto.Model._

final case object RecursiveReferenceConsistency extends AutoVerification {
  val levels = Seq(
    Set(
      RecursiveComponentRefConsistency,
      RecursiveEntityRefConsistency
    ))
}

final case object RecursiveComponentRefConsistency extends Verification {
  def run(model: Model): Seq[RecursiveReferenceError] = {
    val compositeComponents = model.components.collect {
      case c @ CompositeComponent(_, _, _) => c
    }
    compositeComponents
      .flatMap(cc =>
        checkRecursiveComponentRefConsistency(model.components, Seq.empty, cc))
      .toSeq
  }

  private def checkRecursiveComponentRefConsistency(
      components: Set[Component],
      path: Seq[(Reference.Source, Identifier)],
      component: Component): Seq[RecursiveReferenceError] = component match {
    case AtomicComponent(_, _, _, _, _) => Seq.empty
    case CompositeComponent(name, _, children) => {
      val alreadyEncountered = path.find(_._2 == name)
      if (alreadyEncountered.isDefined) {
        Seq(RecursiveReferenceError(path, Reference.Component, name))
      } else {
        val newPath = path :+ ((Reference.CompositeComponent, name))
        children
          .flatMap { c =>
            val child = Reference.resolve(c.component, components).get
            checkRecursiveComponentRefConsistency(components, newPath, child)
          }
      }
    }
  }
}

final case object RecursiveEntityRefConsistency extends Verification {
  def run(model: Model): Seq[RecursiveReferenceError] = {
    model.entities
      .flatMap(e =>
        checkRecursiveEntityRefConsistency(model.entities, Seq.empty, e))
      .toSeq
  }

  private def checkRecursiveEntityRefConsistency(
      entities: Set[Entity],
      path: Seq[(Reference.Source, Identifier)],
      entity: Entity): Seq[RecursiveReferenceError] = {
    entity.attributes.toSeq.flatMap {
      case Variable(_, ref @ EntityRef(name)) => {
        val alreadyEncountered = path.find(_._2 == name)
        if (alreadyEncountered.isDefined) {
          Seq(RecursiveReferenceError(path, Reference.Entity, name))
        } else {
          val newPath = path :+ ((Reference.Entity, name))
          val e       = Reference.resolve(ref, entities).get
          checkRecursiveEntityRefConsistency(entities, newPath, e)
        }
      }
      case Variable(_, _) => Seq.empty
    }
  }
}
