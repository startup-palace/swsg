import org.scalatest._
import wssg._
import wssg.Model._

class CompositeComponentNonEmptinessSpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a composite component has no children" in {
    val m = Model(
      Set.empty,
      Set(
        CompositeComponent("c1", Set.empty, Seq.empty)
      ),
      Seq.empty
    )
    val expectedErrors = Set(
      EmptyCompositeComponentError("c1")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
