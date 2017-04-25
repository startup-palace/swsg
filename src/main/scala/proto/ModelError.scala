package proto

sealed abstract trait ModelError

final case class ComponentNameUnicityError(name: String, occurences: Int)
    extends ModelError
