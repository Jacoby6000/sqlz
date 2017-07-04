package scoobie

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {
  case class Fix[+F[+_]](f: F[Fix[F]])
  case class HFix[F[_[_], _], A](unfix: F[HFix[F, ?], A])

  sealed trait Query[T, A[_], +I]
  trait Value
  trait Comparison

  type QueryValue[T, A[_]] = Query[T, A, Value]
  type QueryComparison[T, A[_]] = Query[T, A, Comparison]

  sealed trait Operator[+I]

  object ValueOperators {
    case object Add extends Operator[Value]
    case object Subtract extends Operator[Value]
    case object Divide extends Operator[Value]
    case object Multiply extends Operator[Value]
  }

  case class Function[T, A[_]](path: Path, args: List[A[Value]]) extends QueryValue[T, A]
  case class ValueBinOp[T, A[_]](left: A[Value], right: A[Value], op: Operator[Value]) extends QueryValue[T, A]
  case class Parameter[T, A[_]](value: T) extends QueryValue[T, A]
  case class PathValue[T, A[_]](path: Path) extends QueryValue[T, A]
  class Null[T, A[_]] extends QueryValue[T, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: Null[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryNull"
  }

  object Null {
    def apply[T, A[_]]: Null[T, A] = new Null[T, A]
  }

  object ComparisonValueOperators {
    case object Equal extends Operator[(Value, Comparison)]
    case object GreaterThan extends Operator[(Value, Comparison)]
    case object GreaterThanOrEqual extends Operator[(Value, Comparison)]
    case object LessThan extends Operator[(Value, Comparison)]
    case object LessThanOrEqual extends Operator[(Value, Comparison)]
  }

  object ComparisonOperators {
    case object And extends Operator[Comparison]
    case object Or extends Operator[Comparison]
  }

  case class Not[T, A[_]](value: A[Comparison]) extends QueryComparison[T, A]
  case class ComparisonBinOp[T, A[_]](left: A[Comparison], right: A[Comparison], op: Operator[Comparison]) extends QueryComparison[T, A]
  case class ComparisonValueBinOp[T, A[_]](left: A[Value], right: A[Value], op: Operator[(Value, Comparison)]) extends QueryComparison[T, A]
  case class In[T, A[_]](left: A[Value], rights: A[Value]) extends QueryComparison[T, A]
  case class Lit[T, A[_]](value: A[Value]) extends QueryComparison[T, A]

  class ComparisonNop[T, A[_]] extends QueryComparison[T, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: ComparisonNop[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryComparisonNop"
  }
  object ComparisonNop {
    def apply[T, A[_]]: ComparisonNop[T, A] = new ComparisonNop[T, A]
  }


  sealed trait Path {
    override def toString: String = this match {
      case PathEnd(p) => p
      case PathCons(p, q) => p + q.toString
    }
  }
  case class PathEnd(path: String) extends Path
  case class PathCons(path: String, queryPath: Path) extends Path

  sealed trait Projection
  sealed trait ProjectOneI extends Projection

  case class ProjectOne[T, A[_]](selection: A[Value], alias: Option[String]) extends Query[T, A, ProjectOneI]
  class ProjectAll[T, A[_]] extends Query[T, A, Projection] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: ProjectAll[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryProjectAll"
  }

  object ProjectAll {
    def apply[T, A[_]]: ProjectAll[T, A] = new ProjectAll[T, A]
  }

  object JoinOperators {
    case object FullOuter extends Operator[Join]
    case object LeftOuter extends Operator[Join]
    case object RightOuter extends Operator[Join]
    case object Cartesian extends Operator[Join]
    case object Inner extends Operator[Join]
  }

  sealed trait Join
  case class QueryJoin[T, A[_]](table: ProjectOne[T, A], on: A[Comparison], op: Operator[Join]) extends Query[T, A, Join]

  sealed trait SortType
  object SortType {
    case object Ascending extends SortType
    case object Descending extends SortType
  }
  case class Sort(column: Path, sortType: SortType)

  case class QuerySelect[T, A[_]](
                                table: A[ProjectOneI],
                                values: List[A[Projection]],
                                joins: List[A[Join]],
                                filter: A[Comparison],
                                sorts: List[Sort],
                                groupings: List[Sort],
                                offset: Option[Long],
                                limit: Option[Long]
  ) extends QueryValue[T, A]

  //case class ModifyField[A](key: QueryPath, value: A)
  //case class QueryInsert[A](collection: QueryPath, values: List[ModifyField[A]])
  //case class QueryUpdate[A, B](collection: QueryPath, values: List[ModifyField[A]], where: QueryComparison[B, A])
  //case class QueryDelete[A, B](collection: QueryPath, where: QueryComparison[A, B])

  type FixedQuery[A] = HFix[Query[A, ?[_], ?], A]
  type QueryOf[T] = {
    type l[F[_], I] = Query[T, F, I]
  }
}

object cata {
  import ast._

  type Algebra[F[_[_], _], E[_]] = F[E, ?] ~> E

  trait HFunctor[H[_[_], _]] {
    // def fmap[F[_]: Functor, A, B](hfa: H[F, A])(f: A => B): H[F, B]
    def hmap[F[_], G[_]](f: F ~> G): H[F, ?] ~> H[G, ?]
  }
  object HFunctor {
    def apply[H[_[_], _]](implicit H: HFunctor[H]) = H

    // implicit def hfunctorFunctor[H[_[_], _]: HFunctor, F[_]: Functor]:
    //     Functor[H[F, ?]] =
    //   new Functor[H[F, ?]] {
    //     def map[A, B](fa: H[F, A])(f: A => B) = HFunctor[H].fmap(fa)(f)
    // }
  }

  trait ~>[F[_], G[_]] {
    def apply[A](fa: F[A]): G[A]
  }

  implicit val hfixRecursive: HRecursive[HFix] =
    new HRecursive[HFix] {
      override def hproject[F[_[_], _], A](t: HFix[F, A]) = t.unfix
    }

  trait HRecursive[T[_[_[_], _], _]] {
    def hproject[F[_[_], _], A](t: T[F, A]): F[T[F, ?], A]

    def cata[F[_[_], _]: HFunctor, A[_]](φ: Algebra[F, A]): T[F, ?] ~> A =
      new (T[F, ?] ~> A) {
        def apply[Q](t: T[F, Q]) =
          φ(HFunctor[F].hmap(cata(φ))(hproject(t)))
      }
  }

  implicit def queryHFunctor[T]: HFunctor[QueryOf[T]#l] = new HFunctor[QueryOf[T]#l] {
    def hmap[F[_], G[_]](f: F ~> G) = new (Query[T, F, ?] ~> Query[T, G, ?]) {
      def apply[I](fa: Query[T, F, I]): Query[T, G, I] =
        fa match {
          case Parameter(param) => Parameter(param)
          case Function(path, args) => Function(path, args.map(hmap(f)(_)))

        }
    }
  }


}

