package proto

import Model.Identifier

sealed abstract trait ModelError

final case class ComponentNameUnicityError(name: Identifier, occurences: Int)
    extends ModelError

final case class EntityNameUnicityError(name: Identifier, occurences: Int)
    extends ModelError

final case class AttributeNameUnicityError(entityName: Identifier,
                                           attributeName: Identifier,
                                           occurences: Int)
    extends ModelError

final case class BrokenReferenceError(
    sourceType: Reference.Source,
    sourceName: Identifier,
    targetType: Reference.Target,
    targetName: Identifier
) extends ModelError
