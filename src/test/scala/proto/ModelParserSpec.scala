import org.scalatest._
import proto._
import proto.Model._
import scala.util.Success

class ModelSpec extends FlatSpec with Matchers {
  "Model parser" should "work with a simple model" in {
    val input = """
e
  name Registration
  attributes (name: String, email: String, date: DateTime)
e
  name CancelledRegistration
  attributes (registration: Registration)

"""
    val model = Model(
      Set(
        Entity("Registration",
               Set(Variable("name", Str),
                   Variable("email", Str),
                   Variable("date", DateTime))),
        Entity("CancelledRegistration",
               Set(Variable("registration", EntityRef("Registration"))))
      ),
      Set.empty,
      Seq.empty
    )
    val parsedModel = ModelParser(input).ModelFile.run()
    parsedModel shouldBe a[Success[_]]
    parsedModel.get shouldBe model
  }
}
