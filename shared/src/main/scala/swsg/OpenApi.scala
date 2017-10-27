package swsg

import OpenApi._

final case class OpenApi(
    openapi: String,
    `x-swsg-version`: String,
    components: Option[Components],
    /*paths: Paths*/)

final case object OpenApi {
  def fromJson(json: String): Either[io.circe.Error, OpenApi] = {
    import io.circe.parser.decode
    import OpenApiInstances.decodeOpenApi

    decode[OpenApi](json).flatMap { spec =>
      if (spec.openapi.substring(0, 3) != "3.0") {
          Left(io.circe.DecodingFailure("This tool only supports OpenAPI 3.0.x", List.empty))
        }
        else if (spec.`x-swsg-version`.substring(0, 3) != "1.0") {
          Left(io.circe.DecodingFailure("This tool only supports SWSG 1.0.x", List.empty))
        }
        else Right(spec)
    }
  }

  final case class Components(schemas: Option[Map[String, SchemaOrRef]])

  sealed abstract trait SchemaOrRef
  final case class Schema(
    title: Option[String],
    multipleOf: Option[Int],
    maximum: Option[Int],
    exclusiveMaximum: Option[Boolean],
    minimum: Option[Int],
    exclusiveMinimum: Option[Boolean],
    maxLength: Option[Int],
    minLength: Option[Int],
    pattern: Option[String],
    maxItems: Option[Int],
    minItems: Option[Int],
    uniqueItems: Option[Boolean],
    maxProperties: Option[Int],
    minProperties: Option[Int],
    required: Option[Seq[String]],
    enum: Option[Seq[io.circe.Json]],
    `type`: Option[String],
    allOf: Option[Seq[SchemaOrRef]],
    oneOf: Option[Seq[SchemaOrRef]],
    anyOf: Option[Seq[SchemaOrRef]],
    not: Option[Seq[SchemaOrRef]],
    items: Option[Schema],
    properties: Option[Map[String, SchemaOrRef]],
    //additionalProperties: Option[Either[Boolean, SchemaOrRef]], // FIXME
    format: Option[String],
    default: Option[io.circe.Json],
    nullable: Option[Boolean],
    discriminator: Option[Discriminator],
    readOnly: Option[Boolean],
    writeOnly: Option[Boolean],
    xml: Option[String],
    externalDocs: Option[ExternalDocumentation],
    example: Option[io.circe.Json],
    deprecated: Option[Boolean],
  ) extends SchemaOrRef

  final case class Discriminator(propertyName: String, mapping: Map[String, String])
  final case class ExternalDocumentation(description: String, url: String)

  //final case class Paths()

  final case class Reference($ref: String) extends SchemaOrRef
}

final case object OpenApiInstances {
  import io.circe.{Decoder, HCursor}
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveDecoder
  //import OpenApi._

  implicit val config: Configuration = Configuration.default

  implicit val decodeDiscriminator: Decoder[Discriminator] = deriveDecoder
  implicit val decodeExternalDocumentation: Decoder[ExternalDocumentation] = deriveDecoder

  implicit val decodeReference: Decoder[Reference] = deriveDecoder
  implicit val decodeSchema: Decoder[Schema] = deriveDecoder

  implicit val decodeSchemaOrRef: Decoder[SchemaOrRef] = new Decoder[SchemaOrRef] {
    final def apply(c: HCursor): Decoder.Result[SchemaOrRef] = {
      decodeReference(c) match {
        case Left(_) => decodeSchema(c)
        case r @ _ => r
      }
    }
  }

  implicit val decodeComponents: Decoder[Components] = deriveDecoder
  implicit val decodeOpenApi: Decoder[OpenApi] = deriveDecoder
}
