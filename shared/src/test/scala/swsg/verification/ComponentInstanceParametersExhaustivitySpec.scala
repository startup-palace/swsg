import org.scalatest._
import swsg._
import swsg.Model._

class ComponentInstanceParametersExhaustivitySpec
    extends FlatSpec
    with Matchers {
  "Model verification" should "fail if a component instance does not provide the right arguments" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set(Variable("p1", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty),
        CompositeComponent(
          "c2",
          Set.empty,
          Seq(
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Constant(Str, "some value"))),
              Set.empty),
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Integer), Constant(Integer, 10))),
              Set.empty)
          )
        )
      ),
      Seq(
        Service("GET",
                "\\/",
                Set.empty,
                ComponentInstance(ComponentRef("c1"), Set.empty, Set.empty)))
    )
    val expectedErrors = Set(
      MissingArgumentError(Reference.CompositeComponent,
                           "c2",
                           "c1",
                           Variable("p1", Str)),
      UselessArgumentError(Reference.CompositeComponent,
                           "c2",
                           "c1",
                           Variable("p1", Integer)),
      MissingArgumentError(Reference.Service,
                           "GET \\/",
                           "c1",
                           Variable("p1", Str))
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }

  it should "succeed if a component instance provide the right arguments using a variable in the scope" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set(Variable("p1", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty),
        CompositeComponent(
          "c2",
          Set(Variable("p2", Str)),
          Seq(
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Variable("p2", Str))),
              Set.empty))
        ),
        CompositeComponent(
          "c3",
          Set.empty,
          Seq(
            ComponentInstance(
              ComponentRef("c2"),
              Set(Binding(Variable("p2", Str), Constant(Str, "some value"))),
              Set.empty))
        )
      ),
      Seq.empty
    )
    val expectedErrors = Set.empty
    val errors         = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }

  it should "fail if a component instance provide a arguments using a variable not in scope" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set(Variable("p1", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty),
        CompositeComponent(
          "c2",
          Set(Variable("p2", Str)),
          Seq(
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Variable("p3", Str))),
              Set.empty))
        ),
        CompositeComponent(
          "c3",
          Set.empty,
          Seq(
            ComponentInstance(
              ComponentRef("c2"),
              Set(Binding(Variable("p2", Str), Constant(Str, "some value"))),
              Set.empty))
        )
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      NotInScopeArgumentError(Reference.CompositeComponent,
                              "c2",
                              "c1",
                              Variable("p1", Str),
                              Variable("p3", Str))
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
