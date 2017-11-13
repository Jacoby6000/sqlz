package scoobie.interpreters.ansi

trait SqlQueryLifter[F[_], B] {

  /**
    * This method is used to lift something of type A, in to the context of B, using something of type F[A].
    *
    * A can be seen as the type of the piece of sql query (a number, a literal, a query fragment)
    * F[A] can be seen as an interpreter for A. So, something that can take A and convert to B
    *
    * B can be seen as the fully interpreted query type.  For example: doobie.util.fragment.Fragment
    *
    */
  def liftValue[A](a: A, fa: F[A]): B

}
