import org.scalatest._
import proto._
import proto.Model._

class ComponentContextVariableNameUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component has several variables with the same name in its specification" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Str), Variable("v2", Str)),
                        Set.empty,
                        Set.empty),
        AtomicComponent("c2",
                        Set.empty,
                        Set(Variable("v1", Str), Variable("v1", Integer)),
                        Set.empty,
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
      ComponentContextVariableNameUnicityError("c2", "v1", 2),
      ComponentContextVariableNameUnicityError("c3", "v1", 2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
