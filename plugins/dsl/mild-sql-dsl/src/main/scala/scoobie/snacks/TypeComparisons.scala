package scoobie.snacks

/**
  * Created by jacob.barber on 3/21/17.
  */
object TypeComparisons {
  /* Stolen From Shapeless */
  def unexpected : Nothing = sys.error("Unexpected invocation")

  // Type inequalities
  trait =:!=[A, B] extends Serializable

  implicit def neq[A, B] : A =:!= B = new =:!=[A, B] {}
  implicit def neqAmbig1[A] : A =:!= A = unexpected
  implicit def neqAmbig2[A] : A =:!= A = unexpected
}
