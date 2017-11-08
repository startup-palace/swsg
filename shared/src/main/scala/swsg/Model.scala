package swsg

import Model._

final case class Model(entities: Set[Entity],
                       components: Set[Component],
                       services: Seq[Service]) {
  lazy val atomicComponents: Set[AtomicComponent] = this.components.collect {
    case ac @ AtomicComponent(_, _, _, _, _) => ac
  }
  lazy val compositeComponents: Set[CompositeComponent] =
    this.components.collect {
      case cc @ CompositeComponent(_, _, _) => cc
    }
}

final case object Model {
  type Identifier = String
  type Method     = String
  type Path       = String

  sealed abstract trait Type
  final case object Str                          extends Type
  final case object Boolean                      extends Type
  final case object Integer                      extends Type
  final case object Float                        extends Type
  final case object Date                         extends Type
  final case object DateTime                     extends Type
  final case class EntityRef(entity: Identifier) extends Type
  final case class SeqOf(seqOf: Type)            extends Type
  final case class OptionOf(optionOf: Type)      extends Type
  final case object Inherited                    extends Type

  final case class Entity(name: Identifier, attributes: Set[Variable])

  sealed abstract trait Component {
    def name: Identifier
    def params: Set[Variable]
  }
  final case class AtomicComponent(name: Identifier,
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
                                     bindings: Set[Binding],
                                     aliases: Set[Alias])
  final case class Binding(param: Variable, argument: Term)
  final case class Alias(source: Identifier, target: Identifier)

  final case class ComponentRef(target: Identifier) extends AnyVal

  sealed abstract trait Term
  final case class Variable(name: Identifier, `type`: Type) extends Term
  final case class Constant(`type`: Type, value: Any)       extends Term

  final case class Service(method: Method,
                           path: Path,
                           params: Set[ServiceParameter],
                           component: ComponentInstance) {
    lazy val name: String = s"${method} ${path}"
  }

  final case class ServiceParameter(location: ParameterLocation, variable: Variable)

  sealed abstract trait ParameterLocation
  final case object Query extends ParameterLocation
  final case object Header extends ParameterLocation
  final case object Path extends ParameterLocation
  final case object Cookie extends ParameterLocation
  final case object Body extends ParameterLocation
}

final case object ModelDecoderInstances {
  import io.circe.{Decoder, HCursor}
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveDecoder

  implicit val config: Configuration = Configuration.default

  implicit val decodeEntityRef: Decoder[EntityRef] = deriveDecoder
  implicit val decodeSeqOf: Decoder[SeqOf] = deriveDecoder
  implicit val decodeOptionOf: Decoder[OptionOf] = deriveDecoder

  val decodeScalarType: Decoder[Type] = new Decoder[Type] {
    final def apply(c: HCursor): Decoder.Result[Type] = {
      c.as[String] match {
        case Right("Str") => Right(Str)
        case Right("Boolean") => Right(Boolean)
        case Right("Integer") => Right(Integer)
        case Right("Float") => Right(Float)
        case Right("Date") => Right(Date)
        case Right("DateTime") => Right(DateTime)
        case Right("Inherited") => Right(Inherited)
        case Right(t) => Left(io.circe.DecodingFailure(s"Type '$t' is not supported", c.history))
        case Left(e) => Left(e)
      }
    }
  }
  implicit val decodeType: Decoder[Type] = new Decoder[Type] {
    final def apply(c: HCursor): Decoder.Result[Type] = {
      val decoders = Seq(
        decodeEntityRef,
        decodeSeqOf,
        decodeOptionOf,
        decodeScalarType,
      )

      val init: Decoder.Result[Type] = Left(io.circe.DecodingFailure("Init", c.history))
      decoders.foldLeft(init) {
        case (acc @ Right(_), _) => acc
        case (Left(_), cur) => cur(c)
      }
    }
  }

  implicit val decodeAny: Decoder[Any] = new Decoder[Any] {
    final def apply(c: HCursor): Decoder.Result[Any] = c.as[String]
  }

  implicit val decodeVariable: Decoder[Variable] = deriveDecoder
  implicit val decodeConstant: Decoder[Constant] = deriveDecoder

  implicit val decodeTerm: Decoder[Term] = new Decoder[Term] {
    final def apply(c: HCursor): Decoder.Result[Term] = {
      decodeVariable(c) match {
        case Left(_) => decodeConstant(c)
        case r @ _ => r
      }
    }
  }

  implicit val decodeComponentRef: Decoder[ComponentRef] = deriveDecoder
  implicit val decodeBinding: Decoder[Binding] = deriveDecoder
  implicit val decodeAlias: Decoder[Alias] = deriveDecoder

  implicit val decodeComponentInstance: Decoder[ComponentInstance] = new Decoder[ComponentInstance] {
    final def apply(c: HCursor): Decoder.Result[ComponentInstance] =
      for {
        component <- c.downField("component").as[ComponentRef]
        bindings <- c.downField("bindings").as[Option[Set[Binding]]]
        aliases <- c.downField("aliases").as[Option[Set[Alias]]]
      } yield {
        ComponentInstance(
          component,
          bindings.getOrElse(Set.empty),
          aliases.getOrElse(Set.empty))
      }
  }

  implicit val decodeAtomicComponent: Decoder[AtomicComponent] = new Decoder[AtomicComponent] {
    final def apply(c: HCursor): Decoder.Result[AtomicComponent] =
      for {
        name <- c.downField("name").as[Identifier]
        params <- c.downField("params").as[Option[Set[Variable]]]
        pre <- c.downField("pre").as[Option[Set[Variable]]]
        add <- c.downField("add").as[Option[Set[Variable]]]
        rem <- c.downField("rem").as[Option[Set[Variable]]]
      } yield {
        AtomicComponent(
          name,
          params.getOrElse(Set.empty),
          pre.getOrElse(Set.empty),
          add.getOrElse(Set.empty),
          rem.getOrElse(Set.empty))
      }
  }
  implicit val decodeCompositeComponent: Decoder[CompositeComponent] = new Decoder[CompositeComponent] {
    final def apply(c: HCursor): Decoder.Result[CompositeComponent] =
      for {
        name <- c.downField("name").as[Identifier]
        params <- c.downField("params").as[Option[Set[Variable]]]
        components <- c.downField("components").as[Seq[ComponentInstance]]
      } yield {
        CompositeComponent(
          name,
          params.getOrElse(Set.empty),
          components)
      }
  }
}
