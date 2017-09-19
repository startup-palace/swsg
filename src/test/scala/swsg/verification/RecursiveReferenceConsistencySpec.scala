import org.scalatest._
import swsg._
import swsg.Model._

class RecursiveReferenceConsistencySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component is referenced from in one of its descendants" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1", Set.empty, Set.empty, Set.empty, Set.empty),
        CompositeComponent(
          "c2",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("c1"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("c3"), Set.empty, Set.empty)
          )
        ),
        CompositeComponent(
          "c3",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("c1"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("c2"), Set.empty, Set.empty)
          )
        ),
        CompositeComponent(
          "c4",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("c4"), Set.empty, Set.empty)
          )
        )
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      RecursiveReferenceError(Seq((Reference.CompositeComponent, "c2"),
                                  (Reference.CompositeComponent, "c3")),
                              Reference.Component,
                              "c2"),
      RecursiveReferenceError(Seq((Reference.CompositeComponent, "c3"),
                                  (Reference.CompositeComponent, "c2")),
                              Reference.Component,
                              "c3"),
      RecursiveReferenceError(Seq((Reference.CompositeComponent, "c4")),
                              Reference.Component,
                              "c4")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }

  it should "fail if an entity is referenced from one of its descendants" in {
    val m = Model(
      Set(
        Entity("e1", Set(Variable("a1", EntityRef("e1")))),
        Entity("e2", Set(Variable("a1", EntityRef("e3")))),
        Entity("e3", Set(Variable("a1", EntityRef("e4")))),
        Entity("e4", Set(Variable("a1", EntityRef("e2"))))
      ),
      Set.empty,
      Seq.empty
    )
    val expectedErrors = Set(
      RecursiveReferenceError(Seq((Reference.Entity, "e1")),
                              Reference.Entity,
                              "e1"),
      RecursiveReferenceError(Seq((Reference.Entity, "e2"),
                                  (Reference.Entity, "e3"),
                                  (Reference.Entity, "e4")),
                              Reference.Entity,
                              "e2"),
      RecursiveReferenceError(Seq((Reference.Entity, "e3"),
                                  (Reference.Entity, "e4"),
                                  (Reference.Entity, "e2")),
                              Reference.Entity,
                              "e3"),
      RecursiveReferenceError(Seq((Reference.Entity, "e4"),
                                  (Reference.Entity, "e2"),
                                  (Reference.Entity, "e3")),
                              Reference.Entity,
                              "e4")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
