package swsg.backend

import better.files._
import swsg.Model
import swsg.Model.{AtomicComponent, Identifier}

final case class Implementation(atomicComponents: Map[Identifier, String])

final case object Implementation {
  def fromPath(model: Model,
               path: String): Either[ImplementationError, Implementation] = {
    val acPath = path / "ac"
    if (acPath.isEmpty) {
      Left(PathNotFound(acPath.toString))
    } else {
      val acFiles = acPath.glob("*.php")
      val acImpl =
        acFiles.map(p => (p.nameWithoutExtension, p.contentAsString)).toMap

      val requiredAcs: Set[Identifier] = model.components
        .collect {
          case ac @ AtomicComponent(_, _, _, _, _) => ac
        }
        .map(_.name)
      val missingAcs = requiredAcs.filter(name => !acImpl.contains(name))

      if (missingAcs.isEmpty) {
        Right(Implementation(acImpl))
      } else {
        Left(MissingAtomicComponentImplementations(missingAcs))
      }
    }
  }
}

sealed abstract trait ImplementationError
final case class PathNotFound(path: String) extends ImplementationError
final case class MissingAtomicComponentImplementations(
    componentNames: Set[Identifier])
    extends ImplementationError
