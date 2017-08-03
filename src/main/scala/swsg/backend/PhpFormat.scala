package swsg.backend

import play.twirl.api._
import scala.collection.immutable

/**
 * Content type used in default PHP templates.
 */
class Php private (elements: immutable.Seq[Php], text: String)
    extends BufferedContent[Php](elements, text) {
  def this(text: String) = this(Nil, Formats.safe(text))
  def this(elements: immutable.Seq[Php]) = this(elements, "")
  val contentType = "application/x-httpd-php"
}

/**
 * Helper for utilities Php methods.
 */
object Php {

  /**
   * Creates a text fragment with initial content specified.
   */
  def apply(text: String): Php = {
    new Php(text)
  }
}

/**
 * Formatter for PHP content.
 */
object PhpFormat extends Format[Php] {

  /**
   * Create a Php fragment.
   */
  def raw(text: String): Php = Php(text)

  /**
   * No need for a safe (escaped) Php fragment.
   */
  def escape(text: String): Php = Php(text)

  /**
   * Generate an empty Php fragment
   */
  val empty: Php = new Php("")

  /**
   * Create an Php Fragment that holds other fragments.
   */
  def fill(elements: immutable.Seq[Php]): Php = new Php(elements)
}
