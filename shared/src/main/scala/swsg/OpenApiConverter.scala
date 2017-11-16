package swsg

import OpenApi._

final case object OpenApiConverter {
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

    def parseSchemaReference(reference: String): ValidatedNel[String, String] = {
      reference.split("/").toList match {
        case "#" :: "components" :: "schemas" :: name :: Nil => name.validNel
        case _ => s"Cannot resolve reference '$reference'".invalidNel
      }
    }

    def resolveSchemaReference(schemas: Map[String, SchemaOrRef])(reference: String): ValidatedNel[String, Schema] = {
      parseSchemaReference(reference).andThen { name =>
        schemas.get(name) match {
          case Some(s: Schema) => s.validNel
          case Some(ref: Reference) => resolveSchemaReference(schemas)(ref.$ref)
          case None => s"Schema '$name' is not defined".invalidNel
        }
      }
    }

    val schemas: Map[String, SchemaOrRef] = openapi
      .components
      .map(_.schemas.getOrElse(Map.empty))
      .getOrElse(Map.empty)

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

      def flattenSchemas(allSchemas: Map[String, SchemaOrRef])(path: Seq[String], schemas: Seq[SchemaOrRef]): ValidatedNel[String, Schema] = {
        val resolved: Seq[ValidatedNel[String, Schema]] = schemas.map {
          case OpenApi.Reference(ref) => resolveSchemaReference(allSchemas)(ref)
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
        case OpenApi.Reference(ref) => s"References are not supported (${path.mkString(" -> ")})".invalidNel
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
        val extractedParameters = parameters.map(extractParameter).sequence.map(_.toSet)
        val extractedBody = o.requestBody.map(b => extractBody(b).map(sp => Set(sp))).getOrElse(Set.empty.validNel)
        (extractedParameters, extractedBody).mapN(_ ++ _)
      }

      def extractBody(b: RequestBodyOrRef): ValidatedNel[String, ServiceParameter] = {
        b match {
          case OpenApi.Reference(_) => "References in operation requestBody are not handled".invalidNel
          case RequestBody(_, _, _, None) => "RequestBody in operation must have a 'x-swsg-name' attribute".invalidNel
          case RequestBody(_, content, required, Some(name)) => {
            if (content.size != 1) {
              "Multiple items in content in RequestBody are not handled".invalidNel
            } else {
              val extractedType = content
                .values
                .head
                .schema
                .map(s => schemaToType(Seq("RequestBody"), s, required.getOrElse(false)))
                .getOrElse("Content items in RequestBody must have a 'schema' attribute".invalidNel)
              extractedType.map(t => ServiceParameter(Body, Variable(name, t)))
            }
          }
        }
      }

      def extractParameter(p: ParameterOrRef): ValidatedNel[String, ServiceParameter] = {
        p match {
          case OpenApi.Reference(_) => "References in operation parameters are not handled".invalidNel
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
          case Some(s) => schemaToType(Seq(s"parameter '${p.name}'"), s, required)
        }
      }

      def schemaToType(parents: Seq[String], schemaOrRef: SchemaOrRef, required: Boolean): ValidatedNel[String, Type] = {
        val computedType: ValidatedNel[String, Type] = schemaOrRef match {
          case OpenApi.Reference(ref) => parseSchemaReference(ref).andThen(n => EntityRef(n).valid)
          case SchemaHasType("integer", _) => Model.Integer.valid
          case SchemaHasType("number", _) => Model.Float.valid
          case SchemaHasType("string", _) => Model.Str.valid
          case SchemaHasType("boolean", _) => Model.Boolean.valid
          case SchemaHasType("object", _) => ???
          case s @ SchemaHasType("array", _) => s.items match {
            case Some(sub) => schemaToType(parents :+ "[list]", sub, true).map(computedSub => SeqOf(computedSub))
            case None => s"Schemas of type 'array' must have an 'items' attribute in '${parents.mkString(" -> ")}'".invalidNel
          }
          case SchemaHasType("null", _) => s"'null' type are not allowed in '${parents.mkString(" -> ")}'".invalidNel
          case _ => s"Schema is not supported in '${parents.mkString(" -> ")}'".invalidNel
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
        .toVector
        .flatMap {
          case (path, pathItem) => pathItem.toOperations.map {
            case (method, operation) => (method.toUpperCase, path, operation)
          }
        }

      val swsgOperations: Seq[(String, String, ComponentInstance, Operation)] = operations.flatMap {
        case (method, path, o @ Operation(_, _, _, _, _, _, _, _, _, Some(ci))) =>
          Vector((method, path, ci, o))
        case (_, _, Operation(_, _, _, _, _, _, _, _, _, None)) => Vector.empty
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
}
