import org.scalatest._
import swsg._
import swsg.Model._

class FlattenerSpec extends FlatSpec with Matchers {
  "Components flattening" should "be straightforward for atomic components" in {
    val ac        = AtomicComponent("ac1", Set.empty, Set.empty, Set.empty, Set.empty)
    val flattener = Flattener(Set(ac))
    val flattened = flattener.flatten(ac)
    flattened shouldBe Seq(ac)
  }

  it should "work for a 2-levels composite component" in {
    def mkEmptyAC(name: Identifier) =
      AtomicComponent(name, Set.empty, Set.empty, Set.empty, Set.empty)
    def mkEmptyCI(c: Component) =
      ComponentInstance(ComponentRef(c.name), Set.empty, Set.empty)
    def mkEmptyCC(name: Identifier, components: Seq[Component]) =
      CompositeComponent(name, Set.empty, components.map(mkEmptyCI))

    val ac1 = mkEmptyAC("ac1")
    val ac2 = mkEmptyAC("ac2")
    val ac3 = mkEmptyAC("ac3")
    val ac4 = mkEmptyAC("ac4")
    val cc1 = mkEmptyCC("cc1", Seq(ac1, ac1))
    val cc2 = mkEmptyCC("cc2", Seq(ac2, ac3, cc1, ac4))

    val flattener = Flattener(Set(ac1, ac2, ac3, ac4, cc1, cc2))
    val flattened = flattener.flatten(cc2)
    val expected  = Seq(ac2, ac3, ac1, ac1, ac4)

    flattened shouldBe expected
  }

  it should "work for a 2-levels composite component and return their parents as well" in {
    def mkEmptyAC(name: Identifier) =
      AtomicComponent(name, Set.empty, Set.empty, Set.empty, Set.empty)
    def mkEmptyCI(c: Component) =
      ComponentInstance(ComponentRef(c.name), Set.empty, Set.empty)
    def mkEmptyCC(name: Identifier, components: Seq[Component]) =
      CompositeComponent(name, Set.empty, components.map(mkEmptyCI))

    val ac1 = mkEmptyAC("ac1")
    val ac2 = mkEmptyAC("ac2")
    val ac3 = mkEmptyAC("ac3")
    val ac4 = mkEmptyAC("ac4")
    val cc1 = mkEmptyCC("cc1", Seq(ac1, ac1))
    val cc2 = mkEmptyCC("cc2", Seq(ac2, ac3, cc1, ac4))

    val flattener = Flattener(Set(ac1, ac2, ac3, ac4, cc1, cc2))
    val flattened = flattener.flattenWithParents(cc2)
    val expected = Seq(
      AtomicComponentWithParents(ac2, Seq("cc2")),
      AtomicComponentWithParents(ac3, Seq("cc2")),
      AtomicComponentWithParents(ac1, Seq("cc2", "cc1")),
      AtomicComponentWithParents(ac1, Seq("cc2", "cc1")),
      AtomicComponentWithParents(ac4, Seq("cc2"))
    )

    flattened shouldBe expected
  }
}
