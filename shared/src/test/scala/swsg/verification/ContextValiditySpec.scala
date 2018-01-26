import org.scalatest._
import swsg._
import swsg.Model._

class ContextValiditySpec extends FlatSpec with Matchers {
  "Model verification" should "succeed in a almost empty model" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1", Set.empty, Set.empty, Set.empty, Set.empty)
      ),
      Seq(
        Service("GET",
                "/",
                Set.empty,
                ComponentInstance("c1", Set.empty, Set.empty))
      )
    )
    val errors = ConsistencyVerification.run(m)

    errors shouldBe empty
  }

  it should "succeed if the contexts given to any component fulfill their preconditions" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Str)),
                        Set(Variable("v2", Integer)),
                        Set.empty),
        AtomicComponent("c2",
                        Set.empty,
                        Set(Variable("v1", Str), Variable("v2", Integer)),
                        Set.empty,
                        Set.empty),
        CompositeComponent(
          "c3",
          Set.empty,
          Seq(ComponentInstance("c1", Set.empty, Set.empty),
              ComponentInstance("c2", Set.empty, Set.empty)))
      ),
      Seq(
        Service("GET",
                "/{v1}",
                Set(ServiceParameter(Path, Variable("v1", Str))),
                ComponentInstance("c3", Set.empty, Set.empty))
      )
    )
    val errors = ConsistencyVerification.run(m)

    errors shouldBe empty
  }

  it should "fail in a simple case of unmatched precondition" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Str)),
                        Set.empty,
                        Set.empty)
      ),
      Seq(
        Service("GET",
                "/",
                Set.empty,
                ComponentInstance("c1", Set.empty, Set.empty))
      )
    )
    val errors = ConsistencyVerification.run(m)
    val expectedErrors = Set(
      ComponentPreconditionError("GET /", Seq("c1"), Variable("v1", Str))
    )

    errors should contain theSameElementsAs expectedErrors
  }

  it should "fail if the contexts given to any component does not fulfill their preconditions" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Str)),
                        Set(Variable("v2", Integer)),
                        Set(Variable("v1", Str))),
        AtomicComponent("c2",
                        Set.empty,
                        Set(Variable("v1", Str), Variable("v2", Integer)),
                        Set.empty,
                        Set.empty),
        CompositeComponent(
          "c3",
          Set.empty,
          Seq(ComponentInstance("c1", Set.empty, Set.empty),
              ComponentInstance("c2", Set.empty, Set.empty)))
      ),
      Seq(
        Service("GET",
                "/{v1}",
                Set(ServiceParameter(Path, Variable("v1", Str))),
                ComponentInstance("c3", Set.empty, Set.empty))
      )
    )
    val errors = ConsistencyVerification.run(m)
    val expectedErrors = Set(
      ComponentPreconditionError("GET /{v1}",
                                 Seq("c3", "c2"),
                                 Variable("v1", Str))
    )

    errors should contain theSameElementsAs expectedErrors
  }

  it should "succeed component instances have the right aliases" in {
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
            ComponentInstance("SanitizeEmail",
                              Set.empty,
                              Set(Alias("email", "email1"),
                                  Alias("sanitizedEmail", "sanitizedEmail1"))),
            ComponentInstance("SanitizeEmail",
                              Set.empty,
                              Set(Alias("email", "email2"),
                                  Alias("sanitizedEmail", "sanitizedEmail2")))
          )
        )
      ),
      Seq(
        Service("GET",
                "/{email1}/{email2}",
                Set(
                  ServiceParameter(Path, Variable("email1", Str)),
                  ServiceParameter(Path, Variable("email2", Str)),
                ),
                ComponentInstance("SanitizeEmails",
                                  Set.empty,
                                  Set.empty)))
    )
    val errors = ConsistencyVerification.run(m)

    errors shouldBe empty
  }
}
