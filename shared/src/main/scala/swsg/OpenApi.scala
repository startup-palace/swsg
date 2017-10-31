package swsg

import OpenApi._

final case class OpenApi(
    openapi: String,
    `x-swsg-version`: String,
    components: Option[Components],
    paths: Map[String, PathItem])

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

  final case class Components(
    schemas: Option[Map[String, SchemaOrRef]],
  )

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
  ) extends SchemaOrRef {
    override def toString: String = {
      val fields = Map(
        "title" -> title,
        "multipleOf" -> multipleOf,
        "maximum" -> maximum,
        "exclusiveMaximum" -> exclusiveMaximum,
        "minimum" -> minimum,
        "exclusiveMinimum" -> exclusiveMinimum,
        "maxLength" -> maxLength,
        "minLength" -> minLength,
        "pattern" -> pattern,
        "maxItems" -> maxItems,
        "minItems" -> minItems,
        "uniqueItems" -> uniqueItems,
        "maxProperties" -> maxProperties,
        "minProperties" -> minProperties,
        "required" -> required,
        "enum" -> enum,
        "type" -> `type`,
        "allOf" -> allOf,
        "oneOf" -> oneOf,
        "anyOf" -> anyOf,
        "not" -> not,
        "items" -> items,
        "properties" -> properties,
        //"additionalProperties" -> additionalProperties,
        "format" -> format,
        "default" -> default,
        "nullable" -> nullable,
        "discriminator" -> discriminator,
        "readOnly" -> readOnly,
        "writeOnly" -> writeOnly,
        "xml" -> xml,
        "externalDocs" -> externalDocs,
        "example" -> example,
        "deprecated" -> deprecated,
      ).toSeq.flatMap {
        case (label, Some(value)) => Seq(s"$label = ${value.toString}")
        case _ => Seq.empty
      }.mkString(", ")
      s"Schema($fields)"
    }
  }

  final case class Discriminator(propertyName: String, mapping: Map[String, String])
  final case class ExternalDocumentation(description: String, url: String)

  final case class PathItem(
    $ref: Option[String],
    summary: Option[String],
    description: Option[String],
    get: Option[Operation],
    put: Option[Operation],
    post: Option[Operation],
    delete: Option[Operation],
    options: Option[Operation],
    head: Option[Operation],
    patch: Option[Operation],
    trace: Option[Operation],
    //servers: Option[Seq[Server]],
    parameters: Option[Seq[ParameterOrRef]],
  )

  sealed abstract trait ParameterOrRef
  final case class Parameter(
    name: String,
    in: String,
    description: Option[String],
    required: Option[Boolean],
    deprecated: Option[Boolean],
    allowEmptyVal: Option[Boolean],
    // simple scenarios
    style: Option[String],
    explode: Option[Boolean],
    allowReserved: Option[Boolean],
    schema: Option[SchemaOrRef],
    //example: Option[io.circe.Json],
    //examples: Option[Map[String, ExampleOrRef]],
    // complex scenarios
    content: Option[Map[String, MediaType]],
  ) extends ParameterOrRef

  final case class MediaType(
    schema: Option[SchemaOrRef],
    //example: Option[io.circe.Json],
    //examples: Option[Map[String, ExampleOrRef]],
    encoding: Option[Map[String, Encoding]],
  )

  final case class Encoding(
    contentType: Option[String],
    headers: Option[Map[String, HeaderOrRef]],
    style: Option[String],
    explode: Option[Boolean],
    allowReserved: Option[Boolean],
  )

  final case class Operation(
    tags: Option[Seq[String]],
    summary: Option[String],
    description: Option[String],
    externalDocs: Option[ExternalDocumentation],
    operationId: Option[String],
    parameters: Option[Seq[ParameterOrRef]],
    //requestBody: Option[RequestBody],
    responses: Map[String, ResponseOrRef],
    //callbacks: Option[Map[String, CallbackOrRef]],
    deprecated: Option[Boolean],
    //security: Option[Seq[SecurityRequirement]],
    //servers: Option[Seq[Server]],
  )

  sealed abstract trait ResponseOrRef
  final case class Response(
    description: String,
    headers: Option[Map[String, HeaderOrRef]],
    content: Option[Map[String, MediaType]],
    //links: Option[Map[String, LinkOrRef]],
  ) extends ResponseOrRef

  sealed abstract trait HeaderOrRef
  final case class Header(
    description: Option[String],
    required: Option[Boolean],
    deprecated: Option[Boolean],
    allowEmptyVal: Option[Boolean],
    // simple scenarios
    style: Option[String],
    explode: Option[Boolean],
    allowReserved: Option[Boolean],
    schema: Option[SchemaOrRef],
    //example: Option[io.circe.Json],
    //examples: Option[Map[String, ExampleOrRef]],
    // complex scenarios
    content: Map[String, MediaType],
  ) extends HeaderOrRef

  final case class Reference($ref: String)
      extends SchemaOrRef
      with ParameterOrRef
      with ResponseOrRef
      with HeaderOrRef
}

final case object OpenApiInstances {
  import io.circe.{Decoder, HCursor}
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveDecoder

  implicit val config: Configuration = Configuration.default

  implicit val decodeDiscriminator: Decoder[Discriminator] = deriveDecoder
  implicit val decodeExternalDocumentation: Decoder[ExternalDocumentation] = deriveDecoder
  implicit val decodeMediaType: Decoder[MediaType] = deriveDecoder
  implicit val decodeEncoding: Decoder[Encoding] = deriveDecoder

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

  implicit val decodeParameter: Decoder[Parameter] = deriveDecoder

  implicit val decodeParameterOrRef: Decoder[ParameterOrRef] = new Decoder[ParameterOrRef] {
    final def apply(c: HCursor): Decoder.Result[ParameterOrRef] = {
      decodeReference(c) match {
        case Left(_) => decodeParameter(c)
        case r @ _ => r
      }
    }
  }

  implicit val decodeResponse: Decoder[Response] = deriveDecoder

  implicit val decodeResponseOrRef: Decoder[ResponseOrRef] = new Decoder[ResponseOrRef] {
    final def apply(c: HCursor): Decoder.Result[ResponseOrRef] = {
      decodeReference(c) match {
        case Left(_) => decodeResponse(c)
        case r @ _ => r
      }
    }
  }

  implicit val decodeHeader: Decoder[Header] = deriveDecoder

  implicit val decodeHeaderOrRef: Decoder[HeaderOrRef] = new Decoder[HeaderOrRef] {
    final def apply(c: HCursor): Decoder.Result[HeaderOrRef] = {
      decodeReference(c) match {
        case Left(_) => decodeHeader(c)
        case r @ _ => r
      }
    }
  }

  implicit val decodeComponents: Decoder[Components] = deriveDecoder
  implicit val decodeOperation: Decoder[Operation] = deriveDecoder
  implicit val decodePathItem: Decoder[PathItem] = deriveDecoder
  implicit val decodeOpenApi: Decoder[OpenApi] = deriveDecoder
}
