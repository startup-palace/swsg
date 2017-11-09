package swsg.backend

import scala.collection.{Map, SortedMap}
import swsg.backend.laravel._
import swsg.Model
import swsg.Model._
import swsg.Reference

final case object Laravel extends Backend {
  val name = "laravel"

  val componentNamespace = """App\Components"""
  val componentBaseDir   = "app/Components/"
  val executeMethod      = "execute"
  val swsgNamespace      = """App\SWSG"""
  val swsgDir            = "app/SWSG/"

  def generate(model: Model, impl: Implementation): Map[String, String] = {
    val routeFile = php.routes(model.components, model.services)
    val atomicComponentFiles = model.atomicComponents
      .map(c => impl.atomicComponents.find(_._1 == c.name).get)
      .map(acImpl => s"${componentBaseDir}/${acImpl._1}.php" -> acImpl._2)
    val compositeComponentFiles = model.compositeComponents.map(
      c =>
        s"${componentBaseDir}/${c.name}.php" -> php
          .compositeComponent(model.components, c))

    val generatedFiles = (Map(
      "routes/generated.php" -> routeFile,
      "app/Providers/GeneratedRouteServiceProvider.php" ->
        php.generatedRouteServiceProvider(),
      s"${swsgDir}/Ctx.php"       -> php.ctx(),
      s"${swsgDir}/Params.php"    -> php.params(),
      s"${swsgDir}/Component.php" -> php.component(),
      s"${swsgDir}/Variable.php"  -> php.variable()
    ) ++ compositeComponentFiles).mapValues(s => render(s))

    SortedMap((generatedFiles ++ atomicComponentFiles).toSeq: _*)
  }

  private def render(tpl: Php): String = tpl.toString.trim ++ "\n"

  def instantiate(cs: Set[Component],
                  ci: ComponentInstance,
                  ctx: String,
                  parentParams: String = "$params"): String = {
    def transformBinding(b: Binding): String = {
      val value = b.argument match {
        case Variable(n, t)       => s"${parentParams}->get('$n', '$t')->value"
        case Constant(Str, v)     => s"'$v'"
        case Constant(Boolean, v) => v.toString
        case Constant(Integer, v) => v.toString
        case Constant(Float, v)   => v.toString
        case Constant(t, v) =>
          throw new RuntimeException(
            s"Parameters of type '$t' are not handled yet!")
      }
      s"new \\${swsgNamespace}\\Variable('${b.param.name}', '${b.param.`type`}', ${value})"
    }
    val params    = ci.bindings.map(transformBinding).mkString(", ")
    val component = Reference.resolve(ci.component, cs).get
    val preInst = component match {
      case AtomicComponent(_, _, pre, add, rem) => {
        pre
          .map(v =>
            ci.aliases.find(_.source == v.name) match {
              case None => ""
              case Some(a) =>
                s"""->unsafeRename("${a.target}", "${a.source}")"""
          })
          .toSet
          .mkString("")
      }
      case _ => ""
    }
    s"\\${componentNamespace}\\${ci.component.target}::${executeMethod}(new \\${swsgNamespace}\\Params([${params}]), ${ctx}${preInst})"
  }

  def postInstanciation(cs: Set[Component], ci: ComponentInstance): String = {
    val component = Reference.resolve(ci.component, cs).get
    component match {
      case AtomicComponent(_, _, pre, add, rem) => {
        val added = add.map(v =>
          ci.aliases.find(_.source == v.name) match {
            case None => ""
            case Some(a) =>
              s"""->unsafeRename("${a.source}", "${a.target}")"""
        })
        val remainingPre = (pre -- rem).map(v =>
          ci.aliases.find(_.source == v.name) match {
            case None => ""
            case Some(a) =>
              s"""->unsafeRename("${a.source}", "${a.target}")"""
        })
        (added ++ remainingPre).toSet.mkString("")
      }
      case _ => ""
    }
  }

  def genValidatorRules(p: ServiceParameter): Seq[String] = {
    final case class Validator(name: String, rules: Seq[String]) {
      lazy val one: Seq[Validator] = Seq(this)

      lazy val toPHP: String = {
        s"'$name' => '${rules.mkString("|")}'"
      }
    }
    final case object Validator {
      def addRulesToFirst(rules: Seq[String],
                          validators: Seq[Validator]): Seq[Validator] = {
        validators.headOption match {
          case Some(h) =>
            Seq(h.copy(rules = h.rules ++ rules)) ++ validators.tail
          case None => validators
        }
      }
    }

    def getValidators(name: String)(t: Type): Seq[Validator] = t match {
      case OptionOf(subtype) =>
        Validator.addRulesToFirst(Seq("nullable"), getValidators(name)(subtype))
      case SeqOf(subtype) =>
        Validator(name, Seq("array")).one ++ getValidators(name + ".*")(subtype)
      case EntityRef(entity) => Seq.empty // FIXME
      case Str               => Validator(name, Seq("string")).one
      case Boolean           => Validator(name, Seq("boolean")).one
      case Integer           => Validator(name, Seq("integer")).one
      case Float             => Validator(name, Seq("numeric")).one
      case Date              => Validator(name, Seq("date")).one
      case DateTime          => Validator(name, Seq("date")).one
      case Inherited         => Seq.empty
    }

    Seq(p.variable.`type`)
      .flatMap(getValidators(p.variable.name))
      .map(_.toPHP)
  }
}
