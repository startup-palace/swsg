package swsg

import OpenApi._

final case class Versions(openapi: String, `x-swsg-version`: String)

final case class OpenApi(
    openapi: String,
    `x-swsg-version`: String,
    components: Option[Components],
    paths: Map[String, PathItem])

final case object OpenApi {
  def fromJson(json: String): Either[Seq[io.circe.Error], OpenApi] = {
    import cats.data.Validated
    import cats.syntax.apply._
    import io.circe.DecodingFailure
    import io.circe.parser.decodeAccumulating
    import OpenApiInstances.{decodeOpenApi, decodeVersions}

    decodeAccumulating[Versions](json)
      .andThen { versions =>
        lazy val openApiVersionError = DecodingFailure(s"This tool only supports OpenAPI 3.0.x (current is ${versions.openapi})", List.empty)
        val openApiVersion = Validated.condNel(versions.openapi.substring(0, 3) == "3.0", true, openApiVersionError)
        val swsgVersionError = DecodingFailure(s"This tool only supports SWSG 1.0.x (current is ${versions.`x-swsg-version`})", List.empty)
        val swsgVersion = Validated.condNel(versions.`x-swsg-version`.substring(0, 3) == "1.0", true, swsgVersionError)
        (openApiVersion, swsgVersion).mapN((_, _) => versions)
      }
      .andThen(_ => decodeAccumulating[OpenApi](json))
      .leftMap(_.toList)
      .toEither
  }

  def toModel(openapi: OpenApi): Either[Seq[String], Model] = {
    import cats.data.ValidatedNel
    import cats.syntax.apply._
    import cats.syntax.validated._
    import Model._

    final object SchemaHasType {
      def unapply(s: Schema): Option[(String, Option[String])] = {
        s.`type`.map(t => (t, s.format))
      }
    }
    final object SchemaHasProps {
      def unapply(s: Schema): Option[(Map[String, SchemaOrRef], Set[String])] = {
        s.properties.map(props => (props, s.required.getOrElse(Set.empty)))
      }
    }
    final object SchemaHasAllOf {
      def unapply(s: Schema): Option[Seq[SchemaOrRef]] = {
        s.allOf
      }
    }
    final object SchemaHasNullable {
      def unapply(s: Schema): Option[Boolean] = {
        s.nullable
      }
    }

    val computeEntities: ValidatedNel[String, Set[Entity]] = {
      import cats.instances.vector._
      import cats.syntax.traverse._

      def toSwsgType(t: String): ValidatedNel[String, Type] = t match {
        case "string" => Str.valid
        case "boolean" => Boolean.valid
        case "number" => Float.valid
        case "integer" => Integer.valid
        case t => s"Type '$t' is not supported".invalidNel
      }

      @annotation.tailrec
      def resolveReference(schemas: Map[String, SchemaOrRef])(reference: String): ValidatedNel[String, Schema] = {
        reference.split("/").toList match {
          case "#" :: "components" :: "schemas" :: name :: Nil => schemas.get(name) match {
            case Some(s: Schema) => s.validNel
            case Some(ref: Reference) => resolveReference(schemas)(ref.$ref)
            case None => s"Schema '$name' is not defined".invalidNel
          }
          case _ => s"Cannot resolve reference '$reference'".invalidNel
        }
      }

      def flattenSchemas(allSchemas: Map[String, SchemaOrRef])(path: Seq[String], schemas: Seq[SchemaOrRef]): ValidatedNel[String, Schema] = {
        val resolved: Seq[ValidatedNel[String, Schema]] = schemas.map {
          case Reference(ref) => resolveReference(allSchemas)(ref)
          case s: Schema => s.valid
        }

        resolved
          .toVector
          .sequence
          .map(_.foldLeft(Schema.empty) {
            case (acc, cur) => acc merge cur
          })
      }

      def computeSchema(schemas: Map[String, SchemaOrRef])(path: Seq[String], schema: SchemaOrRef): ValidatedNel[String, Set[Variable]] = schema match {
        case Reference(ref) => s"References are not supported (${path.mkString(" -> ")})".invalidNel
        case SchemaHasAllOf(allOf) => {
          flattenSchemas(schemas)(path, allOf)
            .andThen(s => computeSchema(schemas)(path, s))
        }
        case SchemaHasProps(properties, required) => {
          properties
            .toVector
            .map {
              case (name, SchemaHasType(t, format)) => toSwsgType(t).map { t =>
                val computedType = if (required.contains(name)) t else OptionOf(t)
                Variable(name, computedType)
              }
              case (name, _) => s"Type of ${(path :+ name).mkString(" -> ")} is a schema which is not supported".invalidNel
            }
            .sequence
            .map(_.toSet)
        }
        case _ => s"The schema of '${path.mkString(" -> ")}' is not supported".invalidNel
      }

      def computeEntity(schemas: Map[String, SchemaOrRef])(name: String, schema: SchemaOrRef): ValidatedNel[String, Entity] = {
        computeSchema(schemas)(Seq(name), schema).map(attrs => Entity(name, attrs))
      }

      val schemas: Map[String, SchemaOrRef] = openapi
        .components
        .map(_.schemas.getOrElse(Map.empty))
        .getOrElse(Map.empty)

      val computedEntities: Vector[ValidatedNel[String, Entity]] = schemas
        .toVector
        .map(s => computeEntity(schemas)(s._1, s._2))

      computedEntities
        .sequence
        .map(_.toSet)
    }

    val computeComponents: ValidatedNel[String, Set[Component]] = {
      openapi
        .components
        .map { c =>
          val acs = c.`x-swsg-ac`.getOrElse(Set.empty)
          val ccs = c.`x-swsg-cc`.getOrElse(Set.empty)
          val cs: Set[Component] = acs ++ ccs
          cs
        }
        .getOrElse(Set.empty)
        .validNel
    }

    val computeServices: ValidatedNel[String, Seq[Service]] = {
      import cats.instances.vector._
      import cats.syntax.traverse._

      def extractParameters(o: Operation): ValidatedNel[String, Set[ServiceParameter]] = {
        val parameters = o.parameters.getOrElse(Seq.empty).toVector
        // TODO: handle body
        parameters.map(extractParameter).sequence.map(_.toSet)
      }

      def extractParameter(p: ParameterOrRef): ValidatedNel[String, ServiceParameter] = {
        p match {
          case Reference(_) => "References in operation parameters are not handled".invalidNel
          case p @ Parameter(name, in, _, _, _, _, _, _, _, _, _) => {
            val supported = Map(
              "query" -> Model.Query,
              "header" -> Model.Header,
              "path" -> Model.Path,
              "cookie" -> Model.Cookie,
            )
            supported.get(in) match {
              case Some(location) => extractParameterType(p).map(t => ServiceParameter(location, Variable(name, t)))
              case None => s"Value in = '$in' of parameter $name is not supported. It must be one of ${supported.mkString(", ")}.".invalidNel
            }
          }
        }
      }

      def extractParameterType(p: Parameter): ValidatedNel[String, Type] = {
        val required = p.required.getOrElse(false)
        p.schema match {
          case None => s"A schema must be specified in parameter '${p.name}'".invalidNel
          case Some(s) => schemaToType(p.name)(s, required)
        }
      }

      def schemaToType(pname: String)(schemaOrRef: SchemaOrRef, required: Boolean): ValidatedNel[String, Type] = {
        val computedType: ValidatedNel[String, Type] = schemaOrRef match {
          case Reference(_) => s"Schema must be literal (not a reference) in parameter '${pname}'".invalidNel
          case SchemaHasType("integer", _) => Model.Integer.valid
          case SchemaHasType("number", _) => Model.Float.valid
          case SchemaHasType("string", _) => Model.Str.valid
          case SchemaHasType("boolean", _) => Model.Boolean.valid
          case SchemaHasType("object", _) => ???
          case s @ SchemaHasType("array", _) => s.items match {
            case Some(sub) => schemaToType(pname)(sub, true).map(computedSub => SeqOf(computedSub))
            case None => s"Schemas of type 'array' must have an 'items' attribute in parameter '${pname}'".invalidNel
          }
          case SchemaHasType("null", _) => s"'null' type are not allowed in parameter '${pname}'".invalidNel
          case _ => s"Schema must have a 'type' attribute in parameter '${pname}'".invalidNel
        }
        computedType.map { t =>
          schemaOrRef match {
            case SchemaHasNullable(true) => Model.OptionOf(t)
            case _ => if (!required) Model.OptionOf(t) else t
          }
        }
      }

      val operations: Seq[(String, String, Operation)] = openapi
        .paths
        .flatMap {
          case (path, pathItem) => pathItem.toOperations.map {
            case (method, operation) => (method.toUpperCase, path, operation)
          }
        }
        .toVector

      val swsgOperations: Seq[(String, String, ComponentInstance, Operation)] = operations.flatMap {
        case (method, path, o @ Operation(_, _, _, _, _, _, _, _, Some(ci))) =>
          Vector((method, path, ci, o))
        case (_, _, Operation(_, _, _, _, _, _, _, _, None)) => Vector.empty
      }

      swsgOperations.map {
        case (method, path, ci, operation) =>
          extractParameters(operation).map(o => Service(method, path, o, ci))
      }.toVector.sequence.map(_.toSeq)
    }

    (computeEntities, computeComponents, computeServices)
      .mapN(Model.apply)
      .leftMap(_.toList)
      .toEither
  }

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
    //requestBody: Option[RequestBody],
    responses: Map[String, ResponseOrRef],
    //callbacks: Option[Map[String, CallbackOrRef]],
    deprecated: Option[Boolean],
    //security: Option[Seq[SecurityRequirement]],
    //servers: Option[Seq[Server]],
    `x-swsg-ci`: Option[Model.ComponentInstance],
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
  import ModelDecoderInstances.{decodeAtomicComponent, decodeComponentInstance, decodeCompositeComponent}

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
  implicit val decodeVersions: Decoder[Versions] = deriveDecoder
}
