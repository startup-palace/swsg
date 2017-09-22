import org.scalatest._
import swsg._
import swsg.Model._

class ComponentPreconditionExhaustivitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a component removes a variable that is not already present in its context" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Str)),
                        Set.empty,
                        Set(Variable("v1", Str))),
        AtomicComponent("c2",
                        Set.empty,
                        Set(Variable("v2", Str)),
                        Set.empty,
                        Set(Variable("v1", Str)))
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      VariableMissingInPreconditionsError("c2", Variable("v1", Str))
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
