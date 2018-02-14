package sqlz.tagless

object ansi {
  case class Path(path: String) extends AnyVal

  trait QueryValue[F[_], A] {
    def param[U: F](u: U): A
    def function(path: Path, args: List[A]): A
    def nul: A
  }

  trait QueryComparison[U, A] {
    // Compositions with values
    def equal(l: U, r: U): A
    def greaterThan(l: U, r: U): A
    def greaterThanOrEqual(l: U, r: U): A
    def lessThan(l: U, r: U): A
    def lessThanOrEqual(l: U, r: U): A
    def in(value: U, compareTo: List[U]): A
    def lit(u: U): A

    // Compositions with other comparisons
    def not(a: A): A
    def and(l: A, r: A): A
    def or(l: A, r: A): A
  }

  trait QueryProjection[U, A] {
    def all: A
    def one(selection: U, alias: Option[Path]): A
  }

  trait QueryJoin[T, U, A] {
    def inner(table: U, condition: T): A
    def fullOuter(table: U, condition: T): A
    def leftOuter(table: U, condition: T): A
    def rightOuter(table: U, condition: T): A
    def cross(table: U, condition: T): A
  }

  trait QuerySort[A] {
    def ascending(queryPath: Path): A
    def descending(queryPath: Path): A
  }

  trait QueryExpression[T, U, V, W, X, A, B] {
    def select(table: U, values: List[U], joins: List[T], filter: Option[V], sorts: List[W], groupings: List[W], offset: Option[Long], limit: Option[Long]): A

    def insert(collection: Path, values: List[(Path, X)]): B
    def update(collection: Path, values: List[(Path, X)], where: V): B
    def delete(collection: Path, where: V): B
  }
}
