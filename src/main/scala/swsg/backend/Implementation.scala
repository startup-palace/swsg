package swsg.backend

import better.files._
import swsg.Model.Identifier

final case class Implementation(atomicComponents: Map[Identifier, String])

final case object Implementation {
  def fromPath(path: String): Either[ImplementationError, Implementation] = {
    val acPath = path / "ac"
    if (acPath.isEmpty) {
      Left(PathNotFound(acPath.toString))
    } else {
      val acFiles = acPath.glob("*.php")
      val acImpl  = acFiles.map(p => (p.toString, p.contentAsString)).toMap
      Right(Implementation(acImpl))
    }
  }
}

sealed abstract trait ImplementationError
final case class PathNotFound(path: String) extends ImplementationError
