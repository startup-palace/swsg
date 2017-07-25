import org.scalatest._
import wssg._
import wssg.Model._

class ComponentInstanceBindingsConsistencySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component instance does not bind a variable and a term of the same type" in {
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
          Set(Variable("p2", Str), Variable("p3", Integer)),
          Seq(
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Constant(Str, "some value")))),
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Constant(Integer, 10)))),
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Variable("p2", Str)))),
            ComponentInstance(
              ComponentRef("c1"),
              Set(Binding(Variable("p1", Str), Variable("p3", Integer))))
          )
        )
      ),
      Seq(
        Service("GET",
                "\\/",
                Set.empty,
                ComponentInstance(
                  ComponentRef("c1"),
                  Set(Binding(Variable("p1", Str), Constant(Integer, 1))))))
    )
    val expectedErrors = Seq(
      IncorrectBindingError(Reference.CompositeComponent,
                            "c2",
                            "c1",
                            Variable("p1", Str),
                            Integer),
      IncorrectBindingError(Reference.CompositeComponent,
                            "c2",
                            "c1",
                            Variable("p1", Str),
                            Integer),
      IncorrectBindingError(Reference.Service,
                            "GET \\/",
                            "c1",
                            Variable("p1", Str),
                            Integer)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
