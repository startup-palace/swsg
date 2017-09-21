import org.scalatest._
import swsg._
import swsg.Model._

class AliasTargetValiditySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if an alias has a target which will be created" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("SanitizeEmail",
                        Set.empty,
                        Set(Variable("email", Str)),
                        Set(Variable("sanitizedEmail", Str)),
                        Set.empty),
        CompositeComponent(
          "SanitizeEmails",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("SanitizeEmail"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("SanitizeEmail"),
                              Set.empty,
                              Set(Alias("email", "sanitizedEmail")))
          )
        )
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      AliasTargetValidityError(Reference.CompositeComponent,
                               "SanitizeEmails",
                               "sanitizedEmail")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
