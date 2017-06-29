package scoobie

/**
  * Created by jacob.barber on 3/7/17.
  */
object coercion {
  trait Coerce[F[_], G[_], A, B]
  object Coerce {
    def apply[F[_], G[_], A, B] = new Coerce[F, G, A, B] {}
  }
}
