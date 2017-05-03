package proto

abstract trait Verification {
  def run(model: Model): Seq[ModelError]
}

abstract trait AutoVerification extends Verification {
  def levels: Seq[Set[Verification]]

  def run(model: Model): Seq[ModelError] =
    levels.foldLeft(Seq.empty[ModelError]) {
      case (Seq(), cur) => cur.toSeq.flatMap(_.run(model))
      case (acc, _)     => acc
    }
}

final case object ConsistencyVerification extends AutoVerification {
  import verification._

  val levels = Seq(
    Set(
      ComponentNameUnicity,
      EntityNameUnicity,
      AttributeNameUnicity,
      ReferenceConsistency
    ),
    Set(
      RecursiveReferenceConsistency,
      ComponentContextVariableNameUnicity
    ),
    Set(
      ComponentContextImmutability,
      ComponentPreconditionExhaustivity,
      ComponentInstanceParametersExhaustivity
    )
  )
}
