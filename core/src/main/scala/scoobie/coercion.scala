package scoobie

object coercion {
  trait Coerce[A, B, C]
  object Coerce {
    def apply[A, B, C](implicit ev: Coerce[A, B, C]) = ev
    def instance[A, B, C] = new Coerce[A, B, C] {}
  }
}
