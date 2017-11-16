package swsg

import OpenApi._

final case class Versions(openapi: String, `x-swsg-version`: String)

final case class OpenApi(
    openapi: String,
    `x-swsg-version`: String,
    components: Option[Components],
    paths: Map[String, PathItem])

final case object OpenApi {
  final case class Components(
    schemas: Option[Map[String, SchemaOrRef]],
    `x-swsg-ac`: Option[Set[Model.AtomicComponent]],
    `x-swsg-cc`: Option[Set[Model.CompositeComponent]],
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
    required: Option[Set[String]],
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
    def merge(s2: Schema): Schema = {
      Schema(
        s2.title.orElse(this.title),
        s2.multipleOf.orElse(this.multipleOf),
        s2.maximum.orElse(this.maximum),
        s2.exclusiveMaximum.orElse(this.exclusiveMaximum),
        s2.minimum.orElse(this.minimum),
        s2.exclusiveMinimum.orElse(this.exclusiveMinimum),
        s2.maxLength.orElse(this.maxLength),
        s2.minLength.orElse(this.minLength),
        s2.pattern.orElse(this.pattern),
        s2.maxItems.orElse(this.maxItems),
        s2.minItems.orElse(this.minItems),
        s2.uniqueItems.orElse(this.uniqueItems),
        s2.maxProperties.orElse(this.maxProperties),
        s2.minProperties.orElse(this.minProperties),
        this.required.map(p1 => p1 ++ s2.required.getOrElse(Set.empty)).orElse(s2.required),
        this.enum.map(p1 => p1 ++ s2.enum.getOrElse(Seq.empty)).orElse(s2.enum),
        s2.`type`.orElse(this.`type`),
        this.allOf.map(p1 => p1 ++ s2.allOf.getOrElse(Seq.empty)).orElse(s2.allOf),
        this.oneOf.map(p1 => p1 ++ s2.oneOf.getOrElse(Seq.empty)).orElse(s2.oneOf),
        this.anyOf.map(p1 => p1 ++ s2.anyOf.getOrElse(Seq.empty)).orElse(s2.anyOf),
        this.not.map(p1 => p1 ++ s2.not.getOrElse(Seq.empty)).orElse(s2.not),
        this.items.map(p1 => p1 merge s2.items.getOrElse(Schema.empty)).orElse(s2.items),
        this.properties.map(p1 => p1 ++ s2.properties.getOrElse(Map.empty)).orElse(s2.properties),
        s2.format.orElse(this.format),
        s2.default.orElse(this.default),
        s2.nullable.orElse(this.nullable),
        s2.discriminator.orElse(this.discriminator),
        s2.readOnly.orElse(this.readOnly),
        s2.writeOnly.orElse(this.writeOnly),
        s2.xml.orElse(this.xml),
        s2.externalDocs.orElse(this.externalDocs),
        s2.example.orElse(this.example),
        s2.deprecated.orElse(this.deprecated),
      )
    }

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
  final case object Schema {
    lazy val empty = Schema(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)
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
  ) {
    lazy val toOperations: Seq[(String, Operation)] = {
      Map(
        "get" -> this.get,
        "put" -> this.put,
        "post" -> this.post,
        "delete" -> this.delete,
        "options" -> this.options,
        "head" -> this.head,
        "patch" -> this.patch,
        "trace" -> this.trace,
      ).toSeq.flatMap {
        case (method, Some(operation)) => Seq((method, operation))
        case (_, None) => Seq.empty
      }
    }
  }

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
    requestBody: Option[RequestBodyOrRef],
    responses: Map[String, ResponseOrRef],
    //callbacks: Option[Map[String, CallbackOrRef]],
    deprecated: Option[Boolean],
    //security: Option[Seq[SecurityRequirement]],
    //servers: Option[Seq[Server]],
    `x-swsg-ci`: Option[Model.ComponentInstance],
  )

  sealed abstract trait RequestBodyOrRef
  final case class RequestBody(
    description: Option[String],
    content: Map[String, MediaType],
    required: Option[Boolean],
    `x-swsg-name`: Option[String],
  ) extends RequestBodyOrRef

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
      with RequestBodyOrRef
      with ResponseOrRef
      with HeaderOrRef
}
