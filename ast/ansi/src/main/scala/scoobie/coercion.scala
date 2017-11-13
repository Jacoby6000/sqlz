package scoobie.ast

/**
  * Created by jacob.barber on 3/7/17.
  */
object coercion {
  trait Coerce[F[_]]
  object Coerce {
    def apply[F[_]] = new Coerce[F] {}
  }
}
