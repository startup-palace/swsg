package swsg

import Model.{Component, Identifier, Entity, EntityRef}

final case object Reference {
  sealed abstract trait Source
  sealed abstract trait Target
  final case object CompositeComponent extends Source
  final case object Service            extends Source
  final case object Component          extends Source with Target
  final case object Entity             extends Source with Target

  def resolve(ref: Identifier, components: Set[Component]): Option[Component] =
    components.find(_.name == ref)

  def resolve(ref: EntityRef, entities: Set[Entity]): Option[Entity] =
    entities.find(_.name == ref.entity)
}
