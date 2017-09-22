import org.scalatest._
import swsg._
import swsg.Model._

class AliasSourceUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several aliases have the same source" in {
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
                              Set(Alias("email", "email1"),
                                  Alias("email", "email3"),
                                  Alias("sanitizedEmail", "sanitizedEmail1"))),
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
      AliasSourceUnicityError(Reference.CompositeComponent,
                              "SanitizeEmails",
                              "email",
                              2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
