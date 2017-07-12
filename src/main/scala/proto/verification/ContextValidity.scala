package proto.verification

import proto._
import proto.Model._

final case object ContextValidity extends Verification {
  final case class AtomicComponentWithContext(parents: Seq[String],
                                              component: AtomicComponent,
                                              contextIn: Set[Variable],
                                              contextOut: Set[Variable])

  def run(model: Model): Seq[ComponentPreconditionError] = {
    val flattener = Flattener(model.components)
    val componentSequences: Seq[(Service, Seq[AtomicComponentWithParents])] =
      model.services.map(s => (s, flattener.flattenWithParents(s.component)))
    val sequencesWithContext = componentSequences.map(computeContexts)
    sequencesWithContext.flatMap(findInconsistencies)
  }

  private def computeContexts(seq: (Service, Seq[AtomicComponentWithParents]))
    : (Service, Seq[AtomicComponentWithContext]) = {
    def computeContext(acc: (Set[Variable], Seq[AtomicComponentWithContext]),
                       cur: AtomicComponentWithParents)
      : (Set[Variable], Seq[AtomicComponentWithContext]) = {
      val (oldCtx, componentsWithContext) = acc
      val newCtx                          = oldCtx -- cur.component.rem ++ cur.component.add
      val componentWithContext =
        AtomicComponentWithContext(cur.parents, cur.component, oldCtx, newCtx)
      (newCtx, componentsWithContext :+ componentWithContext)
    }

    val initialContext = seq._1.params
    val componentsWithContext = seq._2
      .foldLeft((initialContext, Seq.empty[AtomicComponentWithContext]))(
        computeContext)
      ._2
    (seq._1, componentsWithContext)
  }

  private def findInconsistencies(
      seq: (Service, Seq[AtomicComponentWithContext]))
    : Seq[ComponentPreconditionError] = {
    val (service, sequence) = seq
    sequence.flatMap { componentWithContext =>
      val inconsistencies =
        componentWithContext.component.pre
          .filterNot(componentWithContext.contextIn.contains)
      inconsistencies.toSeq.map(
        i =>
          ComponentPreconditionError(
            service.name,
            componentWithContext.parents :+ componentWithContext.component.name,
            i))
    }
  }

}
