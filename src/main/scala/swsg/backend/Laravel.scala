package swsg.backend

import swsg.backend.laravel._
import swsg.Model

final case object Laravel extends Backend {
  val name = "laravel"

  val componentNamespace = """App\Components"""
  val componentBaseDir   = "app/Components/"
  val executeMethod      = "execute"

  def generate(model: Model, impl: Implementation): Map[String, String] = {
    val routeFile = php.routes(model.services)

    Map(
      "routes/generated.php" -> routeFile,
      "app/Providers/GeneratedRouteServiceProvider.php" ->
        php.generatedRouteServiceProvider(),
      "app/Components/Ctx.php" -> php.ctx()
    ).mapValues(s => render(s))
  }

  private def render(tpl: Php): String = tpl.toString.trim ++ "\n"
}
