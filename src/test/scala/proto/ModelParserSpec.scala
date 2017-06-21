import org.scalatest._
import proto._
import proto.Model._

class ModelSpec extends FlatSpec with Matchers {
  "Model parser" should "work with a simple model" in {
    val input = """
e
  name Registration
  attributes (name: String, email: String, date: DateTime)
e
  name CancelledRegistration
  attributes (registration: Registration)

s
  method GET
  url \/
  ci Home
s
  method POST
  url \/register\/(?<name>[^/]+)\/(?<email>[^/]+)
  params (name: String, email: String)
  ci Register

s
  method GET
  url \/attendees\/(?<key>[^/]+)
  params (key: String)
  ci GetAttendees(apiKey: String = "myKey")

ac
  name ValidateEmail
  pre (email: String)
ac
  name CheckDupRegistration
  pre (name: String, email: String)
  rem (name: String)
ac
  name CreateRegistration
  params (whatever: Integer)
  pre (name: String, email: String)
  add (registration: Registration)
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
      Set(
        AtomicComponent("ValidateEmail",
                        Set.empty,
                        Set(Variable("email", Str)),
                        Set.empty,
                        Set.empty),
        AtomicComponent("CheckDupRegistration",
                        Set.empty,
                        Set(Variable("name", Str), Variable("email", Str)),
                        Set.empty,
                        Set(Variable("name", Str))),
        AtomicComponent(
          "CreateRegistration",
          Set(Variable("whatever", Integer)),
          Set(Variable("name", Str), Variable("email", Str)),
          Set(Variable("registration", EntityRef("Registration"))),
          Set.empty
        )
      ),
      Seq(
        Service(
          "GET",
          "\\/",
          Set.empty,
          ComponentInstance(ComponentRef("Home"), Set.empty)
        ),
        Service(
          "POST",
          "\\/register\\/(?<name>[^/]+)\\/(?<email>[^/]+)",
          Set(Variable("name", Str), Variable("email", Str)),
          ComponentInstance(ComponentRef("Register"), Set.empty)
        ),
        Service(
          "GET",
          "\\/attendees\\/(?<key>[^/]+)",
          Set(Variable("key", Str)),
          ComponentInstance(
            ComponentRef("GetAttendees"),
            Set(Binding(Variable("apiKey", Str), Constant(Str, "myKey"))))
        )
      )
    )
    val parsedModel = ModelParser(input).parse
    parsedModel shouldBe a[Right[_, _]]
    parsedModel.right.get shouldBe model
  }
}
