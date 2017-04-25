package proto

abstract trait Verification {
  def run(model: Model): Seq[ModelError]
}

abstract trait AutoVerification extends Verification {
  def levels: Seq[Set[Verification]]

  def run(model: Model): Seq[ModelError] =
    levels.foldLeft(Seq.empty[ModelError]) {
      case (Seq(), cur) => cur.map(_.run(model)).fold(Seq.empty)(_ ++ _)
      case (acc, __)    => acc
    }
}

final case object ConsistencyVerification extends AutoVerification {
  import verification._

  val levels = Seq(
    Set(
      ComponentNameUnicity
    )
  )
}
