import org.scalatest._
import swsg._
import swsg.Model._

class OpenApiSpec extends FlatSpec with Matchers {
  "OpenAPI parser" should "not parse a standard spec" in {
    val spec = OpenApiExamples.standardSpec
    val parsedSpec = OpenApiConverter.fromJson(spec)
    parsedSpec shouldBe a[Left[_, _]]
  }

  it should "parse a swsg spec" in {
    val spec = OpenApiExamples.swsgSpec
    val parsedSpec = OpenApiConverter.fromJson(spec)
    parsedSpec shouldBe a[Right[_, _]]
  }

  it should "be able to output a SWSG model" in {
    val spec = OpenApiExamples.swsgSpec
    val model = Model(
      Set(
        Entity(
          "Pet",
          Set(
            Variable("name", Str),
            Variable("tag", OptionOf(Str)),
            Variable("id", Integer))),
        Entity(
          "NewPet",
          Set(
            Variable("name", Str),
            Variable("tag", OptionOf(Str)))),
        Entity(
          "Error",
          Set(
            Variable("code", Integer),
            Variable("message", Str))),
      ),
      Set(
        AtomicComponent(
          "FetchAllPets",
          Set.empty,
          Set.empty,
          Set(Variable("pets", SeqOf(EntityRef("Pet")))),
          Set.empty),
        AtomicComponent(
          "FilterPetsByTags",
          Set.empty,
          Set(Variable("pets", SeqOf(EntityRef("Pet"))), Variable("tags", OptionOf(SeqOf(Str)))),
          Set(Variable("filteredPets", SeqOf(EntityRef("Pet")))),
          Set.empty),
        AtomicComponent(
          "LimitPets",
          Set.empty,
          Set(Variable("pets", SeqOf(EntityRef("Pet"))), Variable("limit", OptionOf(Integer))),
          Set(Variable("limitedPets", SeqOf(EntityRef("Pet")))),
          Set.empty),
        AtomicComponent(
          "RenderPets",
          Set.empty,
          Set(Variable("pets", SeqOf(EntityRef("Pet")))),
          Set.empty,
          Set.empty),
        AtomicComponent(
          "RenderPet",
          Set.empty,
          Set(Variable("pet", EntityRef("Pet"))),
          Set.empty,
          Set.empty),
        CompositeComponent(
          "GetAllPets",
          Set.empty,
          List(
            ComponentInstance(ComponentRef("FetchAllPets"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("FilterPetsByTags"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("LimitPets"), Set.empty, Set(Alias("pets", "filteredPets"))),
            ComponentInstance(ComponentRef("RenderPets"), Set.empty, Set(Alias("pets", "limitedPets"))),
          )),
        AtomicComponent(
          "CreatePet",
          Set.empty,
          Set(Variable("newPet", EntityRef("NewPet"))),
          Set(Variable("pet", EntityRef("Pet"))),
          Set.empty),
        CompositeComponent(
          "AddPet",
          Set.empty,
          List(
            ComponentInstance(ComponentRef("CreatePet"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("RenderPet"), Set.empty, Set.empty),
          )),
        AtomicComponent(
          "GetPetById",
          Set.empty,
          Set(Variable("id", Integer)),
          Set(Variable("pet", EntityRef("Pet"))),
          Set.empty),
        CompositeComponent(
          "FindPet",
          Set.empty,
          List(
            ComponentInstance(ComponentRef("GetPetById"), Set.empty, Set.empty),
            ComponentInstance(ComponentRef("RenderPet"), Set.empty, Set.empty),
          )),
        AtomicComponent(
          "DeletePet",
          Set.empty,
          Set(Variable("id", Integer)),
          Set.empty,
          Set.empty),
      ),
      Seq(
        Service(
          "GET",
          "/pets",
          Set(
            ServiceParameter(Query, Variable("tags", OptionOf(SeqOf(Str)))),
            ServiceParameter(Query, Variable("limit", OptionOf(Integer))),
          ),
          ComponentInstance(ComponentRef("GetAllPets"), Set.empty, Set.empty)),
        Service(
          "POST",
          "/pets",
          Set(
            ServiceParameter(Body, Variable("newPet", EntityRef("NewPet"))),
          ),
          ComponentInstance(ComponentRef("AddPet"), Set.empty, Set.empty)),
        Service(
          "GET",
          "/pets/{id}",
          Set(
            ServiceParameter(Path, Variable("id", Integer)),
          ),
          ComponentInstance(ComponentRef("FindPet"), Set.empty, Set.empty)),
        Service(
          "DELETE",
          "/pets/{id}",
          Set(
            ServiceParameter(Path, Variable("id", Integer)),
          ),
          ComponentInstance(ComponentRef("DeletePet"), Set.empty, Set.empty)),
      ),
    )

    val parsedModel = OpenApiConverter.fromJson(spec).flatMap(OpenApiConverter.toModel)
    parsedModel shouldBe a[Right[_, _]]
    parsedModel.right.get shouldBe model
  }
}
