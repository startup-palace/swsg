import org.scalatest._
import proto._
import proto.Model._

class ReferenceConsistencySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component reference is incorrect" in {
    val m = Model(
      Set.empty,
      Set(
        AbstractComponent("c1", Set.empty, Set.empty, Set.empty, Set.empty),
        CompositeComponent(
          "c2",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("c1"), Set.empty),
            ComponentInstance(ComponentRef("c3"), Set.empty),
            ComponentInstance(ComponentRef("c1"), Set.empty)
          )
        )
      ),
      Seq(
        Service("GET",
                "\\/",
                Set.empty,
                ComponentInstance(ComponentRef("c4"), Set.empty)))
    )
    val expectedErrors = Set(
      BrokenReferenceError(Reference.CompositeComponent,
                           "c2",
                           Reference.Component,
                           "c3"),
      BrokenReferenceError(Reference.Service,
                           "GET \\/",
                           Reference.Component,
                           "c4")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }

  it should "fail if an entity reference is incorrect" in {
    val m = Model(
      Set(
        Entity("e1", Set(Variable("a1", Integer))),
        Entity("e2",
               Set(Variable("a1", Integer), Variable("a2", EntityRef("e1")))),
        Entity("e3",
               Set(Variable("a1", Integer), Variable("a2", EntityRef("e4"))))
      ),
      Set.empty,
      Seq.empty
    )
    val expectedErrors = Set(
      BrokenReferenceError(Reference.Entity, "e3", Reference.Entity, "e4")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
