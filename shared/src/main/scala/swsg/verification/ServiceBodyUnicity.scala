package swsg.verification

import swsg._
import swsg.Model._

final case object ServiceBodyUnicity extends Verification {
  def run(model: Model): Seq[ServiceBodyUnicityError] = {
    model.services.flatMap(checkService)
  }

  private def checkService(s: Service): Seq[ServiceBodyUnicityError] = {
    val bodyCount = s.params.toVector.filter(_.location == Body).size

    if (bodyCount > 1) {
      Seq(ServiceBodyUnicityError(s.name, bodyCount))
    } else {
      Seq.empty
    }
  }
}
