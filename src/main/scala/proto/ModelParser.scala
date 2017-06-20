package proto

import org.parboiled2._
import Parser.DeliveryScheme.Either

case class ModelParser(input: ParserInput) extends Parser {
  def parse = ModelFile.run().left.map(e => formatError(e))

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
  def Declaration = rule { Entity }
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
  def Attributes = rule {
    LineSeparator ~ Indentation ~ "attributes" ~ WhitespaceSeparator ~ '(' ~ oneOrMore(
      Attribute).separatedBy(ParameterSeparator) ~ ')'
  }
  def Attribute: Rule1[Model.Variable] = rule {
    (AttributeName ~ optional(WhitespaceSeparator) ~ ':' ~ optional(
      WhitespaceSeparator) ~ Type) ~> ((n, t) => Model.Variable(n, t))
  }
  def AttributeName: Rule1[String] = rule {
    capture(
      CharPredicate.LowerAlpha ~ zeroOrMore(
        CharPredicate.Alpha | CharPredicate.Digit))
  }

  // Type
  def Type: Rule1[Model.Type] = rule {
    Identifier ~> ((n: String) => validTypes.getOrElse(n, Model.EntityRef(n)))
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

  // Utils
  def WhitespaceSeparator = rule { oneOrMore(' ') }
  def LineSeparator       = rule { '\n' }
  def LinesSeparator      = rule { oneOrMore('\n') }
  def ParameterSeparator  = rule { zeroOrMore(' ') ~ ',' ~ zeroOrMore(' ') }
  def Indentation         = WhitespaceSeparator
}
