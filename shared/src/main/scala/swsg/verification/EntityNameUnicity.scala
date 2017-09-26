package swsg.verification

import swsg._

final case object EntityNameUnicity extends Verification {
  def run(model: Model): Seq[EntityNameUnicityError] = {
    val entities         = model.entities.toVector.map(_.name)
    val uniqueComponents = model.entities.map(_.name).toVector
    val diff             = entities.diff(uniqueComponents)
    val duplicates       = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq.map(EntityNameUnicityError.tupled)
  }
}
