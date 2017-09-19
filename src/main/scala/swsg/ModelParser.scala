package swsg

import org.parboiled2._
import Parser.DeliveryScheme.Either

case class ModelParser(input: ParserInput) extends Parser {
  // Model
  def ModelFile = rule {
    optional(LinesSeparator) ~ Declarations ~ optional(LinesSeparator) ~ EOI
  }
  def Declarations = rule {
    oneOrMore(Declaration).separatedBy(LinesSeparator) ~> { (ds: Seq[Any]) =>
      val entities = ds.collect {
        case e: Model.Entity => e
      }
      val components = ds.collect {
        case c: Model.Component => c
      }
      val services = ds.collect {
        case s: Model.Service => s
      }
      Model(entities.toSet, components.toSet, services)
    }
  }
  def Declaration = rule {
    Entity | Service | AtomicComponent | CompositeComponent
  }
  def Identifier: Rule1[Model.Identifier] = rule {
    capture(
      CharPredicate.UpperAlpha ~ zeroOrMore(
        CharPredicate.Alpha | CharPredicate.Digit))
  }

  // Entity
  def Entity = rule {
    'e' ~ Name ~ Attributes ~> ((n, as) => Model.Entity(n, as.toSet))
  }
  def Name: Rule1[String] = rule {
    LineSeparator ~ Indentation ~ "name" ~ WhitespaceSeparator ~ Identifier
  }
  def Attributes: Rule1[Seq[Model.Variable]] = rule {
    LineSeparator ~ Indentation ~ "attributes" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
      Variable).separatedBy(ParameterSeparator) ~ ')'
  }

  // Service
  def Service = rule {
    's' ~ Method ~ Url ~ Params ~ Ci ~> ((m,
                                          u,
                                          p,
                                          c) => Model.Service(m, u, p.toSet, c))
  }
  def Method: Rule1[String] = rule {
    LineSeparator ~ Indentation ~ "method" ~ WhitespaceSeparator ~ capture(
      oneOrMore(CharPredicate.UpperAlpha))
  }
  def Url: Rule1[String] = rule {
    LineSeparator ~ Indentation ~ "url" ~ WhitespaceSeparator ~ capture(
      oneOrMore(CharPredicate.All -- '\n'))
  }
  def Params: Rule1[Seq[Model.Variable]] = rule {
    optional(
      LineSeparator ~ Indentation ~ "params" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
        Variable).separatedBy(ParameterSeparator) ~ ')') ~> (
        (ps: Option[Seq[Model.Variable]]) => ps.getOrElse(Seq.empty))
  }
  def Ci: Rule1[Model.ComponentInstance] = rule {
    LineSeparator ~ Indentation ~ "ci" ~ WhitespaceSeparator ~ (Identifier ~ Bindings ~ Aliases) ~> (
        (n: String,
         bs: Seq[Model.Binding],
         as: Seq[Model.Alias]) =>
          Model.ComponentInstance(Model.ComponentRef(n), bs.toSet, as.toSet))
  }

  // Atomic component
  def AtomicComponent = rule {
    "ac" ~ Name ~ Params ~ Pre ~ Add ~ Rem ~> (
        (n,
         p,
         pre,
         add,
         rem) =>
          Model.AtomicComponent(n, p.toSet, pre.toSet, add.toSet, rem.toSet))
  }
  def Pre: Rule1[Seq[Model.Variable]] = rule {
    optional(
      LineSeparator ~ Indentation ~ "pre" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
        Variable).separatedBy(ParameterSeparator) ~ ')') ~> (
        (vs: Option[Seq[Model.Variable]]) => vs.getOrElse(Seq.empty))
  }
  def Add: Rule1[Seq[Model.Variable]] = rule {
    optional(
      LineSeparator ~ Indentation ~ "add" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
        Variable).separatedBy(ParameterSeparator) ~ ')') ~> (
        (vs: Option[Seq[Model.Variable]]) => vs.getOrElse(Seq.empty))
  }
  def Rem: Rule1[Seq[Model.Variable]] = rule {
    optional(
      LineSeparator ~ Indentation ~ "rem" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
        Variable).separatedBy(ParameterSeparator) ~ ')') ~> (
        (vs: Option[Seq[Model.Variable]]) => vs.getOrElse(Seq.empty))
  }

  // Composite component
  def CompositeComponent = rule {
    "cc" ~ Name ~ Params ~ Cis ~> ((n,
                                    p,
                                    cis) =>
                                     Model.CompositeComponent(n, p.toSet, cis))
  }
  def Cis = rule {
    zeroOrMore(Ci)
  }

  // Type
  def Type: Rule1[Model.Type] = rule {
    SeqOf | (Identifier ~> ((n: String) =>
      validTypes.getOrElse(n, Model.EntityRef(n))))
  }
  val validTypes: Map[String, Model.Type] = Map(
    "Str"      -> Model.Str,
    "String"   -> Model.Str,
    "Boolean"  -> Model.Boolean,
    "Bool"     -> Model.Boolean,
    "Integer"  -> Model.Integer,
    "Int"      -> Model.Integer,
    "Float"    -> Model.Float,
    "Date"     -> Model.Date,
    "DateTime" -> Model.DateTime
  )
  def SeqOf: Rule1[Model.Type] = rule {
    "Seq(" ~ Type ~ ")" ~> ((t: Model.Type) => Model.SeqOf(t))
  }
  def Variable: Rule1[Model.Variable] = rule {
    (VariableName ~ optional(WhitespaceSeparator) ~ ':' ~ optional(
      WhitespaceSeparator) ~ Type) ~> ((n, t) => Model.Variable(n, t))
  }
  def VariableName: Rule1[String] = rule {
    capture(
      CharPredicate.LowerAlpha ~ zeroOrMore(
        CharPredicate.Alpha | CharPredicate.Digit))
  }
  def Binding: Rule1[Model.Binding] = rule {
    (VariableName ~ optional(WhitespaceSeparator) ~ '=' ~ optional(
      WhitespaceSeparator) ~ Term) ~> ((n: String,
                                        v: Model.Term) =>
                                         v match {
                                           case v: Model.Constant =>
                                             Model.Binding(
                                               Model.Variable(n, v.`type`),
                                               v)
                                           case Model.Variable(v, t) =>
                                             Model.Binding(Model.Variable(n, t),
                                                           Model.Variable(v, t))
                                         })
  }
  def Bindings: Rule1[Seq[Model.Binding]] = rule {
    optional('(' ~ oneOrMore(Binding).separatedBy(ParameterSeparator) ~ ')') ~> (
        (bs: Option[Seq[Model.Binding]]) => bs.getOrElse(Seq.empty))
  }
  def Alias: Rule1[Model.Alias] = rule {
    (VariableName ~ optional(WhitespaceSeparator) ~ "->" ~ optional(
      WhitespaceSeparator) ~ VariableName) ~> ((source: String,
                                                target: String) =>
                                                 Model.Alias(source, target))
  }
  def Aliases: Rule1[Seq[Model.Alias]] = rule {
    optional('<' ~ oneOrMore(Alias).separatedBy(ParameterSeparator) ~ '>') ~> (
        (as: Option[Seq[Model.Alias]]) => as.getOrElse(Seq.empty))
  }
  def Term: Rule1[Model.Term] = rule { StrConstant | TermVariable }
  def StrConstant: Rule1[Model.Constant] = rule {
    ('"' ~ capture(zeroOrMore("\\\"" | (CharPredicate.All -- '"'))) ~ '"') ~> (
        (v: String) => Model.Constant(Model.Str, v))
  }
  def TermVariable: Rule1[Model.Variable] = rule {
    VariableName ~> ((v: String) => Model.Variable(v, Model.Inherited))
  }

  // Utils
  def WhitespaceSeparator = rule { oneOrMore(' ') }
  def LineSeparator       = rule { '\n' }
  def LinesSeparator      = rule { oneOrMore('\n') }
  def ParameterSeparator  = rule { zeroOrMore(' ') ~ ',' ~ zeroOrMore(' ') }
  def Indentation         = WhitespaceSeparator
}

object ModelParser {
  def parse(input: String): Either[String, Model] = {
    val parser = ModelParser(input)
    parser.ModelFile
      .run()
      .map(resolveInheritedTypes)
      .left
      .map(e => parser.formatError(e, new ErrorFormatter(showTraces = true)))
  }

  private def resolveInheritedTypes(model: Model): Model = {
    val newComponents = model.components.map {
      case cc @ Model.CompositeComponent(_, p, c) => {
        val newC = c.map { ci =>
          val newBindings = ci.bindings.map {
            case b @ Model.Binding(
                  Model.Variable(paramName, Model.Inherited),
                  Model.Variable(termName, Model.Inherited)) => {
              val binding = for {
                paramType <- Reference
                  .resolve(ci.component, model.components)
                  .flatMap(_.params.find(_.name == paramName).map(_.`type`))
                termType <- p.find(_.name == termName).map(_.`type`)
              } yield
                Model.Binding(Model.Variable(paramName, paramType),
                              Model.Variable(termName, termType))
              binding.getOrElse(b)
            }
            case b => b
          }
          ci.copy(bindings = newBindings)
        }
        cc.copy(components = newC)
      }
      case c => c
    }
    model.copy(components = newComponents)
  }
}
