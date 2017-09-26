import org.scalatest._
import swsg._

class ModelVerificationSpec extends FlatSpec with Matchers {
  "Model verification" should "succeed if model is empty" in {
    val m      = Model(Set.empty, Set.empty, Seq.empty)
    val errors = ConsistencyVerification.run(m)

    errors shouldBe Seq.empty
  }
}
