package swsg

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
      ComponentNameUnicity, // Component name uniqueness
      EntityNameUnicity, // Entity name uniqueness
      AttributeNameUnicity, // Same entity’s attributes name uniqueness
      ReferenceConsistency
    ),
    Set(
      RecursiveReferenceConsistency, // Composite components flattenability
      ComponentContextVariableNameUnicity, // Context variable name uniqueness
      CompositeComponentNonEmptiness, // Composite component nonemptiness
      ContextVariablesTypeValidity,
      AliasSourceUnicity,
      AliasTargetUnicity,
      AliasSourceValidity
    ),
    Set(
      ComponentContextImmutability, // Context immutability
      ComponentPreconditionExhaustivity, // Component’s precondition exhaustivity
      ComponentInstanceBindingsConsistency
    ),
    Set(
      ComponentInstanceParametersExhaustivity, // Component instances’ parameters exhaustivity
      ContextValidity // Context validity
    )
  )
}
