package swsg

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello world!")
    val m = Model(Set.empty, Set.empty, Seq.empty)
    println(m)
    println(ConsistencyVerification.run(m))
  }
}
