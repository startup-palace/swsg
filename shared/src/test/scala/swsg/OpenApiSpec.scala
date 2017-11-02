import org.scalatest._
import swsg._
//import swsg.OpenApi._

class OpenApiSpec extends FlatSpec with Matchers {
  "OpenAPI parser" should "not parse a standard spec" in {
    val spec = OpenApiExamples.standardSpec
    val parsedSpec = OpenApi.fromJson(spec)
    parsedSpec shouldBe a[Left[_, _]]
  }

  it should "parse a swsg spec" in {
    val spec = OpenApiExamples.swsgSpec
    val parsedSpec = OpenApi.fromJson(spec)
    parsedSpec shouldBe a[Right[_, _]]
  }
}
