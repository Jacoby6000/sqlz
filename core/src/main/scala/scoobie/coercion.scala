package scoobie

object coercion {
  trait Coerce[T, A[_]]
  object Coerce {
    def apply[T, A[_]](implicit ev: Coerce[T, A]) = ev
    def instance[T, A[_]] = new Coerce[T, A] {}
  }
}
