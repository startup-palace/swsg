import org.scalatest._
import proto._
import proto.Model._

class ComponentContextImmutabilitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component adds a variable already present in its context" in {
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
                        Set(Variable("v1", Str)),
                        Set(Variable("v1", Str)),
                        Set.empty),
        AtomicComponent("c3",
                        Set.empty,
                        Set(Variable("v1", Str)),
                        Set(Variable("v1", Integer)),
                        Set.empty)
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      VariableOverrideError("c2", "v1"),
      VariableOverrideError("c3", "v1")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
