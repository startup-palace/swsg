package swsg

import Model._

final case class AtomicComponentWithParents(component: AtomicComponent,
                                            parents: Seq[Identifier])

final case class Flattener(components: Set[Component]) {

  def flattenWithParents(
      c: Component,
      parents: Seq[Identifier]): Seq[AtomicComponentWithParents] = {
    c match {
      case ac @ AtomicComponent(name, _, _, _, _) =>
        Seq(AtomicComponentWithParents(ac, parents))
      case CompositeComponent(name, _, cs) =>
        cs.flatMap(ci => flattenWithParents(ci, parents :+ name))
    }
  }

  def flattenWithParents(c: Component): Seq[AtomicComponentWithParents] =
    flattenWithParents(c, Seq.empty)

  def flattenWithParents(
      ci: ComponentInstance,
      parents: Seq[Identifier] = Seq.empty): Seq[AtomicComponentWithParents] = {
    val c: Component = Reference.resolve(ci.component, components).get
    val r            = resolveAliases(ci.aliases)(_)
    val resolvedC: Component = c match {
      case cc @ CompositeComponent(_, _, _) => cc
      case AtomicComponent(n, p, pre, add, rem) =>
        AtomicComponent(n, p, r(pre), r(add), r(rem))
    }
    flattenWithParents(resolvedC, parents)
  }

  def flatten(c: Component): Seq[AtomicComponent] = {
    flattenWithParents(c, Seq.empty).map(_.component)
  }

  def flatten(ci: ComponentInstance): Seq[AtomicComponent] = {
    flattenWithParents(ci, Seq.empty).map(_.component)
  }

  private def resolveAliases(aliases: Set[Alias])(
      vars: Set[Variable]): Set[Variable] = {
    vars.map(v =>
      aliases.find(_.source == v.name) match {
        case None    => v
        case Some(a) => Variable(a.target, v.`type`)
    })
  }

}
