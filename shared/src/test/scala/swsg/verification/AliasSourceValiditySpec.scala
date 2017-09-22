import org.scalatest._
import swsg._
import swsg.Model._

class AliasSourceValiditySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if an alias has a source which does not exist" in {
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
            ComponentInstance(
              ComponentRef("SanitizeEmail"),
              Set.empty,
              Set(Alias("email", "email1"),
                  Alias("email1", "email2"),
                  Alias("email2", "email"),
                  Alias("sanitizedEmail", "sanitizedEmail1"))
            ),
            ComponentInstance(ComponentRef("SanitizeEmail"),
                              Set.empty,
                              Set(Alias("email", "email2"),
                                  Alias("sanitizedEmail", "sanitizedEmail2")))
          )
        )
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      AliasSourceValidityError(Reference.CompositeComponent,
                               "SanitizeEmails",
                               "email1"),
      AliasSourceValidityError(Reference.CompositeComponent,
                               "SanitizeEmails",
                               "email2")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
