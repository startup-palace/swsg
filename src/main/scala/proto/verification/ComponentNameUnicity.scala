package proto.verification

import proto._

final case object ComponentNameUnicity extends Verification {
  def run(model: Model): Seq[ComponentNameUnicityError] = {
    val components       = model.components.toVector.map(_.name)
    val uniqueComponents = model.components.map(_.name).toVector
    val diff             = components.diff(uniqueComponents)
    val duplicates       = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq.map(ComponentNameUnicityError.tupled)
  }
}
