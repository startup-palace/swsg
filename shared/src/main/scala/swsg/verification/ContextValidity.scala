package swsg.verification

import swsg._
import swsg.Model._

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

    val initialContext = seq._1.params.map(_.variable)
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
          .filterNot(containsCompatibleVariable(componentWithContext.contextIn))
      inconsistencies.toSeq.map(
        i =>
          ComponentPreconditionError(
            service.name,
            componentWithContext.parents :+ componentWithContext.component.name,
            i))
    }
  }

  private def containsCompatibleVariable(context: Set[Variable])(
      variable: Variable): Boolean = {
    def removeOptional(t: Type): Type = t match {
      case OptionOf(st) => st
      case _            => t
    }

    def isOptional(t: Type): Boolean = t match {
      case OptionOf(_) => true
      case _           => false
    }

    if (isOptional(variable.`type`)) {
      val sameName = context.filter(_.name == variable.name)
      val candidates = sameName.filter(
        ctxVar =>
          ctxVar.`type` == variable.`type` || ctxVar.`type` == removeOptional(
            variable.`type`))
      sameName.isEmpty || !candidates.isEmpty
    } else {
      context.contains(variable)
    }
  }

}
