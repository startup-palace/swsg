import org.scalatest._
import swsg._
import swsg.Model._

class ServiceParhValiditySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if a service has not exactly the same path parameters as those actually in the path" in {
    val m = Model(
      Set.empty,
      Set(
        AtomicComponent("Test", Set.empty, Set.empty, Set.empty, Set.empty)
      ),
      Seq(
        Service(
          "GET",
          "/path/{test}/other_path/{test3}",
          Set(
            ServiceParameter(Path, Variable("test", Str)),
            ServiceParameter(Path, Variable("test2", Str)),
          ),
          ComponentInstance("Test", Set.empty, Set.empty)
        )
      )
    )
    val expectedErrors = Set(
      MissingParameterInPathError("GET /path/{test}/other_path/{test3}",
                                  "test2"),
      UnknownParameterInPathError("GET /path/{test}/other_path/{test3}",
                                  "test3"),
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
