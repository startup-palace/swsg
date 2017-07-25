import org.scalatest._
import wssg._
import wssg.Model._

class AttributeNameUnicitySpec extends FlatSpec with Matchers {
  "Model verification" should "fail if several attributes of the same entity have the same name" in {
    val m = Model(
      Set(
        Entity("e1",
               Set(
                 Variable("a1", Str),
                 Variable("a1", Boolean),
                 Variable("a1", Integer)
               )),
        Entity("e2",
               Set(
                 Variable("a2", Integer),
                 Variable("a3", Str),
                 Variable("a3", Boolean)
               )),
        Entity("e3", Set(Variable("a4", Str)))
      ),
      Set.empty,
      Seq.empty
    )
    val expectedErrors = Set(
      AttributeNameUnicityError("e1", "a1", 3),
      AttributeNameUnicityError("e2", "a3", 2)
    )
    val errors = ConsistencyVerification.run(m)

    errors should contain theSameElementsAs expectedErrors
  }
}
