import org.scalatest._
import swsg._
import swsg.Model._

class AliasTargetUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several aliases have the same target" in {
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
                                  Alias("sanitizedEmail", "email1"))),
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
      AliasTargetUnicityError(Reference.CompositeComponent,
                              "SanitizeEmails",
                              "email1",
                              2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
