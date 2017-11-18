import org.scalatest._
import swsg._
import swsg.Model._

class ServiceBodyUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several parameters of the same service have the same name" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("Test", Set.empty, Set.empty, Set.empty, Set.empty)
      ),
      Seq(
        Service(
          "GET",
          "/",
          Set(
            ServiceParameter(Body, Variable("test", Str)),
            ServiceParameter(Path, Variable("test2", Integer)),
            ServiceParameter(Body, Variable("test3", Integer)),
          ),
          ComponentInstance("Test", Set.empty, Set.empty)
        )
      )
    )
    val expectedErrors = Set(
      ServiceBodyUnicityError("GET /",
                              2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
