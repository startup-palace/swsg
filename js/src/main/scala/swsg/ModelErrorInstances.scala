package swsg

final case object ModelErrorInstances {
  import io.circe.Encoder
  import io.circe.Json
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveEncoder
  import ModelInstances.{encodeType, encodeVariable}

  implicit val config: Configuration =
    Configuration.default.withDiscriminator("errorName")

  def toStringEncoder[T]: Encoder[T] = new Encoder[T] {
    final def apply(a: T): Json = Json.fromString(a.toString)
  }

  implicit val encodeReferenceSource: Encoder[Reference.Source] =
    toStringEncoder
  implicit val encodeReferenceTarget: Encoder[Reference.Target] =
    toStringEncoder
  implicit val encodeContextElement: Encoder[ContextElement] = toStringEncoder
  implicit val encodeModelError: Encoder[ModelError]         = deriveEncoder
}
