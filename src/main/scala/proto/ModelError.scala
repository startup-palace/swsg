package proto

import Model.{Identifier, Variable}

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
