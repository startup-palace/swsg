package swsg

final case object ModelInstances {
  import io.circe.Encoder
  import io.circe.Json
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveEncoder
  import Model._

  implicit val config: Configuration =
    Configuration.default.withDiscriminator("caseClassName")

  implicit val encodeType: Encoder[Type] = new Encoder[Type] {
    import Model._
    final def apply(a: Type): Json = a match {
      case SeqOf(item: Type) =>
        Json.obj("type" -> Json.fromString("SeqOf"), "item" -> apply(item))
      case EntityRef(target: Identifier) =>
        Json.obj("type"   -> Json.fromString("EntityRef"),
                 "target" -> Json.fromString(target))
      case t => Json.obj("type" -> Json.fromString(t.toString))
    }
  }

  implicit val encodeConstant: Encoder[Constant] = new Encoder[Constant] {
    final def apply(a: Constant): Json =
      Json.obj("type"  -> encodeType(a.`type`),
               "value" -> Json.fromString(a.value.toString))
  }

  implicit val encodeComponentRef: Encoder[ComponentRef] =
    new Encoder[ComponentRef] {
      final def apply(a: ComponentRef): Json = Json.fromString(a.target)
    }

  implicit val encodeParameterLocation: Encoder[ParameterLocation] =
    new Encoder[ParameterLocation] {
      final def apply(a: ParameterLocation): Json = Json.fromString(a.toString)
    }

  implicit val encodeVariable: Encoder[Variable] = deriveEncoder
  implicit val encodeTerm: Encoder[Term]         = deriveEncoder
  implicit val encodeBinding: Encoder[Binding]   = deriveEncoder
  implicit val encodeAlias: Encoder[Alias]       = deriveEncoder
  implicit val encodeComponentInstance: Encoder[ComponentInstance] =
    deriveEncoder
  implicit val encodeAtomicComponent: Encoder[AtomicComponent] = deriveEncoder
  implicit val encodeCompositeComponent: Encoder[CompositeComponent] =
    deriveEncoder
  implicit val encodeComponent: Encoder[Component]               = deriveEncoder
  implicit val encodeServiceParameter: Encoder[ServiceParameter] = deriveEncoder
  implicit val encodeService: Encoder[Service]                   = deriveEncoder
  implicit val encodeEntity: Encoder[Entity]                     = deriveEncoder
  implicit val encodeModel: Encoder[Model]                       = deriveEncoder
}
