package proto

import Model._

final case class Model(entities: Set[Entity],
                       components: Set[Component],
                       services: Seq[Service])

object Model {
  type Identifier = String
  type Method     = String
  type Url        = String

  sealed abstract trait Type
  final case class String()   extends Type
  final case class Boolean()  extends Type
  final case class Integer()  extends Type
  final case class Float()    extends Type
  final case class Date()     extends Type
  final case class DateTime() extends Type

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
                                      components: List[ComponentInstance])
      extends Component
  final case class ComponentInstance(component: Component,
                                     bindings: Set[Binding])
  final case class Binding(param: Variable, argument: Term)

  sealed abstract trait Term
  final case class Variable(name: Identifier, `type`: Type) extends Term
  final case class Constant(`type`: Type, value: Any)       extends Term

  final case class Service(method: Method,
                           url: Url,
                           params: Set[Variable],
                           component: ComponentInstance)
}
