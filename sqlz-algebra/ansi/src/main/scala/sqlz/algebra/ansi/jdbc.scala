package sqlz.algebra.ansi

import sqlz.tagless.ansi._
import java.sql.{Connection, PreparedStatement}
import java.io.InputStream
import scalaz._

object jdbc {

  //TODO: Newts for safety.

  object algebras {
    class QueryValueFragmentAlg[F[_], A](fragment: Fragment[F, A]) extends QueryValue[F, A] {
      import fragment.syntax._

      def param[T: F](t: T): A = t.prepare
      def function(path: Path, args: List[A]): A =
        args match {
          case Nil => path.path.raw ++ sql"()"
          case h :: t => path.path.raw ++ sql"(" ++ t.foldLeft(h)((acc, arg) => acc ++ sql", " ++ arg) ++ sql")"
        }

      def nul: A = sql"NULL"
    }

    class QueryComparisonFragmentAlg[F[_], A](fragment: Fragment[F, A]) extends QueryComparison[A, A] {
      import fragment.syntax._

      def equal(l: A, r: A): A = l ++ sql" = " ++ r
      def greaterThan(l: A, r: A): A = l ++ sql" > " ++ r
      def greaterThanOrEqual(l: A, r: A): A = l ++ sql" >= " ++ r
      def lessThan(l: A, r: A): A = l ++ sql" < " ++ r
      def lessThanOrEqual(l: A, r: A): A = l ++ sql" <= " ++ r
      def in(value: A, compareTo: List[A]): A =
        compareTo match {
          case Nil => sql"1=0" // crude optimization. Goes away when I introduce State
          case h :: t => value ++ sql" IN (" ++ t.foldLeft(h)((acc, arg) => acc ++ sql", " ++ arg) ++ sql")"
        }

      def lit(u: A): A = u

      def not(a: A): A = sql"NOT " ++ a
      def and(l: A, r: A): A = l ++ sql" AND " ++ r
      def or(l: A, r: A): A = l ++ sql" OR " ++ r
    }
  }

  class QueryProjectionFragmentAlg[F[_], A](fragment: Fragment[F, A]) extends QueryProjection[A, A] {
    import fragment.syntax._

    def all: A =
      sql"*"

    def one(selection: A, alias: Option[Path]): A =
      selection ++ alias.map(aliasPath => sql" AS " ++ aliasPath.path.raw).getOrElse(sql"")
  }

  class QueryJoinFragmentAlg[F[_], A](fragment: Fragment[F, A]) extends QueryJoin[A, A, A] {
    import fragment.syntax._

    def buildJoin(joinString: A, table: A, condition: A): A = joinString ++ table ++ sql" ON " ++ condition

    def inner(table: A, condition: A): A = buildJoin(sql" INNER JOIN ", table, condition)
    def fullOuter(table: A, condition: A): A = buildJoin(sql" FULL OUTER JOIN ", table, condition)
    def leftOuter(table: A, condition: A): A = buildJoin(sql" LEFT OUTER JOIN ", table, condition)
    def rightOuter(table: A, condition: A): A = buildJoin(sql" RIGHT OUTER JOIN ", table, condition)
    def cross(table: A, condition: A): A = buildJoin(sql" INNER JOIN ", table, condition)
  }

  class QuerySortFragmentAlg[F[_], A](fragment: Fragment[F, A]) extends QuerySort[A] {
    import fragment.syntax._

    def ascending(queryPath: Path): A = queryPath.path.raw + sql" ASC"
    def descending(queryPath: Path): A = queryPath.path.raw + sql" DESC"
  }

  class QueryExpressionFragmentAlg[F[_], A] extends QueryExpression[A, A, A, A, A, A, A] {
    import fragment.syntax._

    def select(table: A, values: NonEmptyList[A], joins: List[A], filter: Option[A], sorts: List[A], groupings: List[A], offset: Option[Long], limit: Option[Long]): A = {

      val valuesSql = values.tail.foldLeft(values.head)((acc, elem) => acc ++ sql", " ++ elem)

      val baseQuery = sql"SELECT " ++ valuesSql ++ sql" FROM " ++ table

      baseQuery ++
        filter.map(where => baseQuery ++ sql" WHERE " ++ where).getOrElse(sql"")
    }

    def insert(collection: Path, values: List[(Path, A)]): A =
    def update(collection: Path, values: List[(Path, A)], where: A): A
    def delete(collection: Path, where: A): A
  }

  trait Fragment[F[_], A] { self =>
    def prepare[T: F](t: T): A
    def query(str: String): A
    def concat(l: A, r: A): A

    object syntax {

      val semigroup = semigroupFromFragment(self)

      implicit class FragmentOps(a: A) {
        def ++(other: A): A = concat(a, other)
      }

      implicit class StringOps(s: String) {
        def raw: A = query(s)
      }

      implicit class TOps[T](t: T) {
        def prepare(implicit F: F[T]): A = self.prepare(t)
      }

      // maybe get rid of this and just implicitly convert to A given T and implicit F[T].
      // Not sure yet... Would avoid extra boxing, but might be dangerous.
      // Bsaically I'm stealing the strategy used for the show interpolator in scalaz.
      case class Prepped[T] private (t: T, prepper: F[T]) { def prep: A = prepare(t)(prepper) }
      implicit def toPrepped[T: F](t: T): Prepped[T] = Prepped(t, implicitly)

      implicit class StringContextExtensions(ctx: StringContext) {
        // This is terrible, but I'm feeling lazy.
        def interpolator(args: Seq[A]): A =
          (args zip ctx.parts).foldLeft(query("")) {
            case (acc, (param, quer)) =>
              concat(acc, concat(param, query(quer)))
          }

        def sql(args: Prepped[_]*): A = interpolator(args.map(_.prep))
      }
    }
  }

  def semigroupFromFragment[F[_], A](fragment: Fragment[F, A]): Semigroup[A] =
    Semigroup.instance(fragment.concat(_, _))

  object algebra {
    // TODO: Get rid of this intermediate structure.  It should not be necessary; I just can't work out how to get rid of it yet.
    sealed trait PreparableSql {
      /**
       * Produces the PreparedStatement for this query, without the prepared parameters applied.
       */
      val unpreparedStatement: Connection => PreparedStatement = conn =>
        conn.prepareStatement(sqlString)

      val preparedStatement: Connection => PreparedStatement = conn => gatherPreparations.foldLeft(unpreparedStatement(conn))((stmt, f) => f(stmt))

      lazy val gatherPreparations: List[PreparedStatement => PreparedStatement] = {
        def gatherWithIndex(preparableSql: PreparableSql): List[(PreparedStatement, Int) => PreparedStatement] =
          this match {
            case Prepare(f) => List(f)
            case Combine(l, r) => gatherWithIndex(l) ++ gatherWithIndex(r)
            case QueryString(_) => List()
          }

        gatherWithIndex(this).zipWithIndex.map { case (f, idx) => f((_: PreparedStatement), idx) }
      }

      lazy val sqlString: String =
        this match {
          case Prepare(_) => "?"
          case Combine(l, r) => l.sqlString + r.sqlString
          case QueryString(s) => s
        }
    }

    case class Prepare(f: (PreparedStatement, Int) => PreparedStatement) extends PreparableSql
    case class Combine(l: PreparableSql, r: PreparableSql) extends PreparableSql
    case class QueryString(s: String) extends PreparableSql

    trait Preparable[A] {
      def prepare(a: A): (PreparedStatement, Int) => PreparedStatement
      def contramap[B](f: B => A): Preparable[B] = Preparable.instance[B]((stmt, idx, b) => prepare(f(b))(stmt, idx))
    }

    object Preparable {
      def apply[A: Preparable]: Preparable[A] = implicitly[Preparable[A]]

      @inline def instance[A](f: (PreparedStatement, Int, A) => PreparedStatement): Preparable[A] =
        new Preparable[A] {
          def prepare(a: A): (PreparedStatement, Int) => PreparedStatement = f(_, _, a)
        }

      @inline def instanceU[A](f: (PreparedStatement, Int, A) => Unit): Preparable[A] =
        new Preparable[A] {
          def prepare(a: A): (PreparedStatement, Int) => PreparedStatement = (stmt, n) => {f(stmt, n, a); stmt}
        }

      def from[A, B: Preparable](f: A => B): Preparable[A] = Preparable[B].contramap(f)

      implicit val preparableBoolean: Preparable[Boolean] = instanceU(_.setBoolean(_, _))
      implicit val preparableByte: Preparable[Byte] = instanceU(_.setByte(_, _))
      implicit val preparableShort: Preparable[Short] = instanceU(_.setShort(_, _))
      implicit val preparableInt: Preparable[Int] = instanceU(_.setInt(_, _))
      implicit val preparableLong: Preparable[Long] = instanceU(_.setLong(_, _))
      implicit val preparableFloat: Preparable[Float] = instanceU(_.setFloat(_, _))
      implicit val preparableDouble: Preparable[Double] = instanceU(_.setDouble(_, _))
      implicit val preparableString: Preparable[String] = instanceU(_.setString(_, _))

      implicit val preparableRef: Preparable[java.sql.Ref] = instanceU(_.setRef(_, _))
      implicit val preparableTime: Preparable[java.sql.Time] = instanceU(_.setTime(_, _))
      implicit val preparableDate: Preparable[java.sql.Date] = instanceU(_.setDate(_, _))
      implicit val preparableTimestamp: Preparable[java.sql.Timestamp] = instanceU(_.setTimestamp(_, _))

      implicit val preparableInputStream: Preparable[InputStream] = instanceU(_.setBlob(_, _))
    }

    object SqlFragmentAlg extends Fragment[Preparable, PreparableSql] {
      def prepare[T: Preparable](t: T): PreparableSql = Prepare(Preparable[T].prepare(t))
      def query(str: String): PreparableSql = QueryString(str)
      def concat(l: PreparableSql, r: PreparableSql): PreparableSql = Combine(l, r)
    }
  }
}
