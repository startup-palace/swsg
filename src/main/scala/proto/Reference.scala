package proto

import Model.{Component, ComponentRef, Entity, EntityRef}

final case object Reference {
  sealed abstract trait Source
  sealed abstract trait Target
  final case object CompositeComponent extends Source
  final case object Service            extends Source
  final case object Component          extends Source with Target
  final case object Entity             extends Source with Target

  def resolve(ref: ComponentRef,
              components: Set[Component]): Option[Component] =
    components.find(_.name == ref.target)

  def resolve(ref: EntityRef, components: Set[Entity]): Option[Entity] =
    components.find(_.name == ref.target)
}
