package scoobie.snacks.mild.sql

trait QueryType[F[_], B] {
  def toQueryType[A](a: A, fa: F[A]): B
}

object QueryType {
  def apply[F[_], B](implicit ev: QueryType[F, B]): QueryType[F, B] = ev
}
