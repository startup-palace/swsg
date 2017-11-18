import org.scalatest._
import swsg._
import swsg.Model._

class ServiceParameterNameUnicitySpec extends FlatSpec with Matchers {
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
            ServiceParameter(Path, Variable("test", Str)),
            ServiceParameter(Path, Variable("test", Integer)),
            ServiceParameter(Header, Variable("test", Str)),
          ),
          ComponentInstance("Test", Set.empty, Set.empty)
        )
      )
    )
    val expectedErrors = Set(
      ServiceParameterNameUnicityError(
                              "GET /",
                              "test",
                              3)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
