package swsg.verification

import swsg._
import swsg.Model._

final case object AttributeNameUnicity extends Verification {
  def run(model: Model): Seq[AttributeNameUnicityError] = {
    model.entities.toSeq.flatMap(checkEntity)
  }

  private def checkEntity(entity: Entity): Seq[AttributeNameUnicityError] = {
    val attributes       = entity.attributes.toVector.map(_.name)
    val uniqueComponents = entity.attributes.map(_.name).toVector
    val diff             = attributes.diff(uniqueComponents)
    val duplicates       = diff.groupBy(identity).mapValues(_.size + 1)
    duplicates.toSeq
      .map {
        case (attribute, occurences) => (entity.name, attribute, occurences)
      }
      .map(AttributeNameUnicityError.tupled)
  }
}
