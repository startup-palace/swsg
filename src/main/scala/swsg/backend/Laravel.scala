package swsg.backend

import swsg.backend.laravel._
import swsg.Model

final case object Laravel extends Backend {
  val name = "laravel"

  def generate(model: Model, impl: Implementation): Map[String, String] = {
    val routeFile = php.routes(model.services).toString

    Map(
      "routes/generated.php" -> routeFile,
    )
  }
}
