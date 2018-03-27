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
      ComponentNameUnicity, // Component name unicity
      EntityNameUnicity, // Entity name unicity
      AttributeNameUnicity, // Attribute name unicity
      ServiceParameterNameUnicity, // Service parameter name unicity
      ServiceBodyUnicity, // Parameters of location body unicity
      ReferenceConsistency, // Reference consistency
      ComponentContextVariableNameUnicity, // Component context variable name unicity
      CompositeComponentNonEmptiness, // Composite component nonemptiness
      ContextVariablesTypeValidity, // **Implementation details**
      AliasSourceUnicity, // Alias source unicity
      AliasTargetUnicity // Alias target unicity
    ),
    Set(
      RecursiveReferenceConsistency, // Recursive reference consistency
      AliasSourceValidity, // Alias source validity
      AliasTargetValidity, // Alias target validity
      ServicePathValidity, // Service path validity
      ComponentContextImmutability, // Component context immutability
      ComponentPreconditionExhaustivity, // Component precondition exhaustivity
      ComponentInstanceBindingsConsistency, // Component instance bindings consistency
      ComponentInstanceParametersExhaustivity, // Component instance parameters exhaustivity
      ContextValidity // Context validity
    )
  )
}
