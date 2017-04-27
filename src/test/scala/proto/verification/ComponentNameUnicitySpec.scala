import org.scalatest._
import proto._
import proto.Model._

class ComponentNameUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several components have the same name" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1", Set.empty, Set.empty, Set.empty, Set.empty),
        AtomicComponent("c1",
                        Set(Variable("p1", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty),
        AtomicComponent("c2", Set.empty, Set.empty, Set.empty, Set.empty),
        AtomicComponent("c3", Set.empty, Set.empty, Set.empty, Set.empty),
        AtomicComponent("c3",
                        Set(Variable("p1", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty),
        AtomicComponent("c3",
                        Set(Variable("p2", Str)),
                        Set.empty,
                        Set.empty,
                        Set.empty)
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      ComponentNameUnicityError("c1", 2),
      ComponentNameUnicityError("c3", 3)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
