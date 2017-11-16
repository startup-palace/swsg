package swsg

import OpenApi._

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

  implicit val decodeRequestBody: Decoder[RequestBody] = deriveDecoder

  implicit val decodeRequestBodyOrRef: Decoder[RequestBodyOrRef] = new Decoder[RequestBodyOrRef] {
    final def apply(c: HCursor): Decoder.Result[RequestBodyOrRef] = {
      decodeReference(c) match {
        case Left(_) => decodeRequestBody(c)
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
