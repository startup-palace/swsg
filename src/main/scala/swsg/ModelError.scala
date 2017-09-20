package swsg

import Model.{Identifier, Type, Variable}

sealed abstract trait ModelError

final case class ComponentNameUnicityError(name: Identifier, occurences: Int)
    extends ModelError

final case class EntityNameUnicityError(name: Identifier, occurences: Int)
    extends ModelError

final case class AttributeNameUnicityError(entityName: Identifier,
                                           attributeName: Identifier,
                                           occurences: Int)
    extends ModelError

final case class BrokenReferenceError(
    sourceType: Reference.Source,
    sourceName: Identifier,
    targetType: Reference.Target,
    targetName: Identifier
) extends ModelError

final case class RecursiveReferenceError(
    path: Seq[(Reference.Source, Identifier)],
    targetType: Reference.Target,
    targetName: Identifier)
    extends ModelError

final case class ComponentContextVariableNameUnicityError(
    componentName: Identifier,
    variableName: Identifier,
    occurences: Int)
    extends ModelError

final case class EmptyCompositeComponentError(name: Identifier)
    extends ModelError

final case class AliasSourceUnicityError(instanceParentType: Reference.Source,
                                         instanceParentName: Identifier,
                                         name: Identifier,
                                         occurences: Int)
    extends ModelError

final case class AliasTargetUnicityError(instanceParentType: Reference.Source,
                                         instanceParentName: Identifier,
                                         name: Identifier,
                                         occurences: Int)
    extends ModelError

final case class AliasSourceValidityError(instanceParentType: Reference.Source,
                                          instanceParentName: Identifier,
                                          aliasSource: Identifier)
    extends ModelError

sealed abstract trait ContextElement
object ContextElement {
  final case object AtomicComponent extends ContextElement
  final case object Service         extends ContextElement
}

final case class InheritedTypeInContext(elementType: ContextElement,
                                        elementName: Identifier,
                                        variableName: Identifier)
    extends ModelError

final case class VariableOverrideError(componentName: Identifier,
                                       variableName: Identifier)
    extends ModelError

final case class VariableMissingInPreconditionsError(componentName: Identifier,
                                                     variable: Variable)
    extends ModelError

final case class MissingArgumentError(instanceParentType: Reference.Source,
                                      instanceParentName: Identifier,
                                      component: Identifier,
                                      parameter: Variable)
    extends ModelError

final case class UselessArgumentError(instanceParentType: Reference.Source,
                                      instanceParentName: Identifier,
                                      component: Identifier,
                                      argument: Variable)
    extends ModelError

final case class NotInScopeArgumentError(instanceParentType: Reference.Source,
                                         instanceParentName: Identifier,
                                         component: Identifier,
                                         argument: Variable,
                                         variable: Variable)
    extends ModelError

final case class IncorrectBindingError(instanceParentType: Reference.Source,
                                       instanceParentName: Identifier,
                                       component: Identifier,
                                       expected: Variable,
                                       found: Type)
    extends ModelError

final case class ComponentPreconditionError(serviceName: String,
                                            componentStack: Seq[Identifier],
                                            notFulfilled: Variable)
    extends ModelError
