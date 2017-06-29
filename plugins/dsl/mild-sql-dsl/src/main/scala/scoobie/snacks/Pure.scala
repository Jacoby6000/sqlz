package scoobie.snacks

trait Pointed[F[_]] {
  def point[A](a: A): F[A]
}

object Pointed {
  def apply[F[_]](implicit p: Pointed[F]): Pointed[F] = p

  implicit class PointedOps[A](val a: A) extends AnyVal {
    def point[F[_]](implicit ev: Pointed[F]): F[A] = ev.point(a)
  }
}
