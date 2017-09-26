package swsg.backend

import scala.collection.Map
import swsg.Model

/** A backend to generate a web application from a [[Model]].*/
abstract trait Backend {

  /** The internal name of the backend.*/
  def name: String

  /** The generation function.
   *
   * @param model A model of web application.
   * @param impl An implementation.
   * @return A Map of paths to file contents.
   */
  def generate(model: Model, impl: Implementation): Map[String, String]
}

final case object Backend {

  /** A header message that must appear in generated files. */
  def header: String = "This is a generated file, do not edit"
}
