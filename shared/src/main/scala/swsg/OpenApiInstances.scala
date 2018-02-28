package swsg

import OpenApi._
import scala.collection.immutable.ListMap

final case object OpenApiInstances {
  import io.circe.{Decoder, DecodingFailure, HCursor}
  import io.circe.generic.extras.Configuration
  import io.circe.generic.extras.semiauto.deriveDecoder
  import ModelDecoderInstances.{
    decodeAtomicComponent,
    decodeComponentInstance,
    decodeCompositeComponent
  }

  implicit val config: Configuration = Configuration.default

  implicit val decodeDiscriminator: Decoder[Discriminator] = deriveDecoder
  implicit val decodeExternalDocumentation: Decoder[ExternalDocumentation] =
    deriveDecoder
  implicit val decodeMediaType: Decoder[MediaType] = deriveDecoder
  implicit val decodeEncoding: Decoder[Encoding]   = deriveDecoder

  implicit val decodeReference: Decoder[Reference] = deriveDecoder
  implicit val decodeSchema: Decoder[Schema]       = deriveDecoder

  implicit val decodeSchemaOrRef: Decoder[SchemaOrRef] =
    new Decoder[SchemaOrRef] {
      final def apply(c: HCursor): Decoder.Result[SchemaOrRef] = {
        decodeReference(c) match {
          case Left(_) => decodeSchema(c)
          case r @ _   => r
        }
      }
    }

  implicit val decodeParameter: Decoder[Parameter] = deriveDecoder

  implicit val decodeParameterOrRef: Decoder[ParameterOrRef] =
    new Decoder[ParameterOrRef] {
      final def apply(c: HCursor): Decoder.Result[ParameterOrRef] = {
        decodeReference(c) match {
          case Left(_) => decodeParameter(c)
          case r @ _   => r
        }
      }
    }

  implicit val decodeRequestBody: Decoder[RequestBody] = deriveDecoder

  implicit val decodeRequestBodyOrRef: Decoder[RequestBodyOrRef] =
    new Decoder[RequestBodyOrRef] {
      final def apply(c: HCursor): Decoder.Result[RequestBodyOrRef] = {
        decodeReference(c) match {
          case Left(_) => decodeRequestBody(c)
          case r @ _   => r
        }
      }
    }

  implicit val decodeResponse: Decoder[Response] = deriveDecoder

  implicit val decodeResponseOrRef: Decoder[ResponseOrRef] =
    new Decoder[ResponseOrRef] {
      final def apply(c: HCursor): Decoder.Result[ResponseOrRef] = {
        decodeReference(c) match {
          case Left(_) => decodeResponse(c)
          case r @ _   => r
        }
      }
    }

  implicit val decodeHeader: Decoder[Header] = deriveDecoder

  implicit val decodeHeaderOrRef: Decoder[HeaderOrRef] =
    new Decoder[HeaderOrRef] {
      final def apply(c: HCursor): Decoder.Result[HeaderOrRef] = {
        decodeReference(c) match {
          case Left(_) => decodeHeader(c)
          case r @ _   => r
        }
      }
    }

  implicit val decodeComponents: Decoder[Components] = deriveDecoder
  implicit val decodeOperation: Decoder[Operation]   = deriveDecoder

  implicit val decodePathItem: Decoder[PathItem] = new Decoder[PathItem] {
    final def apply(c: HCursor): Decoder.Result[PathItem] = {
      def getOperations(c: HCursor, fields: Vector[String])
        : Decoder.Result[ListMap[String, Operation]] = {
        val empty: Decoder.Result[ListMap[String, Operation]] = Right(
          ListMap.empty)
        val operations = fields
          .map(method => (method, c.downField(method).as[Operation]))
          .foldLeft(ListMap.empty[String, Decoder.Result[Operation]])(_ + _)
          .foldLeft(empty) {
            case (e @ Left(_), _)           => e
            case (Right(acc), (_, Left(e))) => Left(e)
            case (Right(acc), (method, Right(op))) =>
              Right(acc + (method -> op))
          }
        operations
      }

      for {
        ref         <- c.downField("$ref").as[Option[String]]
        summary     <- c.downField("summary").as[Option[String]]
        description <- c.downField("description").as[Option[String]]
        parameters  <- c.downField("parameters").as[Option[Seq[ParameterOrRef]]]
        fields <- c.keys
          .map(_.toVector)
          .toRight(
            DecodingFailure("Current element should be an object", c.history))
        ops <- getOperations(c, fields)
      } yield {
        PathItem(ref, summary, description, ops, parameters)
      }
    }
  }

  implicit val decodeOpenApi: Decoder[OpenApi] = deriveDecoder

  implicit val decodeVersions: Decoder[Versions] = deriveDecoder
}
