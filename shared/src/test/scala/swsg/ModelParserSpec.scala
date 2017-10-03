import org.scalatest._
import swsg._
import swsg.Model._

class ModelSpec extends FlatSpec with Matchers {
  "Model parser" should "work with a simple model" in {
    val input =
      """
e
  name Registration
  attributes (name: String, email: String, date: DateTime, test: Seq(String))
e
  name CancelledRegistration
  attributes (registration: Registration)

cc
  name Registration
  ci ValidateEmail
  ci CheckDupRegistration
  ci CreateRegistration
  ci SaveRegistration
  ci RegistrationSerializer
cc
  name GetAttendees
  params (apiKey: String)
  ci CheckKey(correctKey = apiKey)
  ci FetchRegistrations
  ci RegistrationsSerializer

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
  ci GetAttendees(apiKey = "myKey")

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

ac
  name CheckKey
  params (correctKey: Str)
  pre (key: String)
"""
    val model = Model(
      Set(
        Entity("Registration",
               Set(Variable("name", Str),
                   Variable("email", Str),
                   Variable("date", DateTime),
                   Variable("test", SeqOf(Str)))),
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
        ),
        AtomicComponent(
          "CheckKey",
          Set(Variable("correctKey", Str)),
          Set(Variable("key", Str)),
          Set.empty,
          Set.empty
        ),
        CompositeComponent(
          "Registration",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("ValidateEmail"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("CheckDupRegistration"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("CreateRegistration"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("SaveRegistration"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("RegistrationSerializer"),
                              Set.empty,
                              Set.empty)
          )
        ),
        CompositeComponent(
          "GetAttendees",
          Set(Variable("apiKey", Str)),
          Seq(
            ComponentInstance(ComponentRef("CheckKey"),
                              Set(Binding(Variable("correctKey", Str),
                                          Variable("apiKey", Str))),
                              Set.empty),
            ComponentInstance(ComponentRef("FetchRegistrations"),
                              Set.empty,
                              Set.empty),
            ComponentInstance(ComponentRef("RegistrationsSerializer"),
                              Set.empty,
                              Set.empty)
          )
        )
      ),
      Seq(
        Service(
          "GET",
          "\\/",
          Set.empty,
          ComponentInstance(ComponentRef("Home"), Set.empty, Set.empty)
        ),
        Service(
          "POST",
          "\\/register\\/(?<name>[^/]+)\\/(?<email>[^/]+)",
          Set(Variable("name", Str), Variable("email", Str)),
          ComponentInstance(ComponentRef("Register"), Set.empty, Set.empty)
        ),
        Service(
          "GET",
          "\\/attendees\\/(?<key>[^/]+)",
          Set(Variable("key", Str)),
          ComponentInstance(
            ComponentRef("GetAttendees"),
            Set(Binding(Variable("apiKey", Str), Constant(Str, "myKey"))),
            Set.empty)
        )
      )
    )
    val parsedModel = ModelParser.parse(input)
    parsedModel shouldBe a[Right[_, _]]
    parsedModel.right.get shouldBe model
  }

  it should "handle component instance aliases" in {
    val input =
      """
cc
  name SanitizeEmails
  ci SanitizeEmail<email -> email1, sanitizedEmail -> sanitizedEmail1>
  ci SanitizeEmail<email -> email2, sanitizedEmail -> sanitizedEmail2>

ac
  name SanitizeEmail
  pre (email: String)
  add (sanitizedEmail: String)
"""
    val model = Model(
      Set.empty,
      Set(
        AtomicComponent("SanitizeEmail",
                        Set.empty,
                        Set(Variable("email", Str)),
                        Set(Variable("sanitizedEmail", Str)),
                        Set.empty),
        CompositeComponent(
          "SanitizeEmails",
          Set.empty,
          Seq(
            ComponentInstance(ComponentRef("SanitizeEmail"),
                              Set.empty,
                              Set(Alias("email", "email1"),
                                  Alias("sanitizedEmail", "sanitizedEmail1"))),
            ComponentInstance(ComponentRef("SanitizeEmail"),
                              Set.empty,
                              Set(Alias("email", "email2"),
                                  Alias("sanitizedEmail", "sanitizedEmail2")))
          )
        )
      ),
      Seq.empty
    )
    val parsedModel = ModelParser.parse(input)
    parsedModel shouldBe a[Right[_, _]]
    parsedModel.right.get shouldBe model
  }
}