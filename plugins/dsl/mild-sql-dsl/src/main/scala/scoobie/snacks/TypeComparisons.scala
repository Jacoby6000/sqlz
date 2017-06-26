package scoobie.snacks

/**
  * Created by jacob.barber on 3/21/17.
  */
object TypeComparisons {
  /* Stolen From Shapeless */
  // $COVERAGE-OFF$This cannot be executed.
  def unexpected : Nothing = sys.error("Unexpected invocation")
  // $COVERAGE-ON$
  // Type inequalities
  trait =:!=[A, B] extends Serializable

  implicit def neq[A, B] : A =:!= B = new =:!=[A, B] {}
  // $COVERAGE-OFF$This cannot be executed.
  implicit def neqAmbig1[A] : A =:!= A = unexpected
  implicit def neqAmbig2[A] : A =:!= A = unexpected
  // $COVERAGE-ON$
}
