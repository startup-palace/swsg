package proto

import Model.{Component, ComponentRef}

final case object Reference {
  sealed abstract trait Source
  sealed abstract trait Target
  final case object CompositeComponent extends Source
  final case object Service            extends Source
  final case object Component          extends Target
  final case object Entity             extends Source with Target

  def resolve(ref: ComponentRef,
              components: Set[Component]): Option[Component] =
    components.find(_.name == ref.target)
}
