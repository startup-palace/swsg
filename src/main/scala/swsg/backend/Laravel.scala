package swsg.backend

import scala.collection.{Map, SortedMap}
import swsg.backend.laravel._
import swsg.Model
import swsg.Model._

final case object Laravel extends Backend {
  val name = "laravel"

  val componentNamespace = """App\Components"""
  val componentBaseDir   = "app/Components/"
  val executeMethod      = "execute"
  val swsgNamespace      = """App\SWSG"""
  val swsgDir            = "app/SWSG/"

  def generate(model: Model, impl: Implementation): Map[String, String] = {
    val routeFile = php.routes(model.services)
    val atomicComponentFiles = model.components
      .collect {
        case AtomicComponent(name, _, _, _, _) =>
          impl.atomicComponents.find(_._1 == name).get
      }
      .map(acImpl => s"${componentBaseDir}/${acImpl._1}.php" -> acImpl._2)
    val compositeComponentFiles = model.components
      .collect {
        case c @ CompositeComponent(name, _, _) =>
          s"${componentBaseDir}/${name}.php" -> php.compositeComponent(c)
      }

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

  def instantiate(ci: ComponentInstance,
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
    val params = ci.bindings.map(transformBinding).mkString(", ")
    s"\\${componentNamespace}\\${ci.component.target}::${executeMethod}(${ctx}, new \\${swsgNamespace}\\Params([${params}]))"
  }
}
