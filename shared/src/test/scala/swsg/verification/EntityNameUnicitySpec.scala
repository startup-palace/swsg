import org.scalatest._
import swsg._
import swsg.Model._

class EntityNameUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several entities have the same name" in {
    val m = Model(
      Set(
        Entity("e1", Set(Variable("a1", Str))),
        Entity("e1", Set(Variable("a2", Str))),
        Entity("e2", Set(Variable("a3", Str))),
        Entity("e1", Set(Variable("a4", Str))),
        Entity("e3", Set(Variable("a5", Str))),
        Entity("e2", Set(Variable("a6", Str)))
      ),
      Set.empty,
      Seq.empty
    )
    val expectedErrors = Set(
      EntityNameUnicityError("e1", 3),
      EntityNameUnicityError("e2", 2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
