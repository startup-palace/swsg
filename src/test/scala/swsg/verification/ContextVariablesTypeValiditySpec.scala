import org.scalatest._
import swsg._
import swsg.Model._

class ContextVariablesTypeValiditySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a variable in context as an Inherited type" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("c1",
                        Set.empty,
                        Set(Variable("v1", Inherited)),
                        Set(Variable("v2", Inherited)),
                        Set(Variable("v3", Inherited)))
      ),
      Seq(
        Service("GET",
                "\\/",
                Set(Variable("v4", Inherited)),
                ComponentInstance(ComponentRef("c1"), Set.empty)))
    )
    val expectedErrors = Set(
      InheritedTypeInContext(ContextElement.AtomicComponent, "c1", "v1"),
      InheritedTypeInContext(ContextElement.AtomicComponent, "c1", "v2"),
      InheritedTypeInContext(ContextElement.AtomicComponent, "c1", "v3"),
      InheritedTypeInContext(ContextElement.Service, "GET \\/", "v4")
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
