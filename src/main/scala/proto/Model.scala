package proto

import Model._

final case class Model(entities: Set[Entity],
                       components: Set[Component],
                       services: Seq[Service])

final case object Model {
  type Identifier = String
  type Method     = String
  type Url        = String

  sealed abstract trait Type
  final case object Str      extends Type
  final case object Boolean  extends Type
  final case object Integer  extends Type
  final case object Float    extends Type
  final case object Date     extends Type
  final case object DateTime extends Type

  final case class Entity(name: Identifier, attributes: Set[Variable])
      extends Type

  sealed abstract trait Component {
    def name: Identifier
    def params: Set[Variable]
  }
  final case class AbstractComponent(name: Identifier,
                                     params: Set[Variable],
                                     pre: Set[Variable],
                                     add: Set[Variable],
                                     rem: Set[Variable])
      extends Component
  final case class CompositeComponent(name: Identifier,
                                      params: Set[Variable],
                                      components: Seq[ComponentInstance])
      extends Component
  final case class ComponentInstance(component: ComponentRef,
                                     bindings: Set[Binding])
  final case class Binding(param: Variable, argument: Term)

  final case class ComponentRef(target: Identifier) extends AnyVal

  sealed abstract trait Term
  final case class Variable(name: Identifier, `type`: Type) extends Term
  final case class Constant(`type`: Type, value: Any)       extends Term

  final case class Service(method: Method,
                           url: Url,
                           params: Set[Variable],
                           component: ComponentInstance) {
    lazy val name: String = s"${method} ${url}"
  }
}
