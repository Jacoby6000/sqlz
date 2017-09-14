package scoobie

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {
  case class HFix[F[_[_], _], A](unfix: F[[B] => HFix[F, B], A])

  sealed trait Query[T, A[_], +I]

  object Indicies {
    trait Value
    trait Comparison

    trait ModifyFieldI

    trait Projection
    trait ProjectOneI extends Projection

    trait Join
  }

  import Indicies._

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
  case class Parameter[T, A[_]](value: T) extends Query[T, A, Value]
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
  case class In[T, A[_]](left: A[Value], rights: List[A[Value]]) extends QueryComparison[T, A]
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


  type QueryProjection[T, A[_]] = Query[T, A, Projection]
  type QueryProjectOne[T, A[_]] = Query[T, A, ProjectOneI]

  case class ProjectOne[T, A[_]](selection: A[Value]) extends QueryProjectOne[T, A]
  case class ProjectAlias[T, A[_]](selection: A[ProjectOneI], alias: String) extends QueryProjectOne[T, A]
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

  case class QueryJoin[T, A[_]](table: A[ProjectOneI], on: A[Comparison], op: Operator[Join]) extends Query[T, A, Join]

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
    offset: Option[T],
    limit: Option[T]
  ) extends QueryValue[T, A]

  case class ModifyField[T, A[_]](key: Path, value: A[Value]) extends Query[T, A, ModifyFieldI]
  case class QueryInsert[T, A[_]](collection: Path, values: List[A[ModifyFieldI]]) extends Query[T, A, Unit]
  case class QueryUpdate[T, A[_]](collection: Path, values: List[A[ModifyFieldI]], where: A[Comparison]) extends Query[T, A, Unit]
  case class QueryDelete[T, A[_]](collection: Path, where: A[Comparison]) extends Query[T, A, Unit]

}

object cata {
  import ast._

  trait LiftH[F[_[_[_], _], _]] {
    def lift[G[_[_], _], I](g: G[[A] => F[G, A], I]): F[G, I]
  }

  trait LiftAST[A[_], G[_[_], _]] {
    def lift[I](g: G[A, I]): A[I]
  }

  def hfixLiftQuery[G[_[_], _]]: LiftAST[[A] => HFix[G, A], G] =
    new LiftAST[[A] => HFix[G, A], G] {
      def lift[I](g: G[[A] => HFix[G, A], I]): HFix[G, I] = HFix(g)
    }

  implicit val hfixLiftH: LiftH[HFix] = new LiftH[HFix] {
    def lift[G[_[_], _], I](g: G[[A] => HFix[G, A], I]): HFix[G, I] = HFix(g)
  }

  type LiftQueryAST[T, A[_]] = LiftAST[A, [F[_], I] => Query[T, F, I]]

  type Algebra[F[_[_], _], E[_]] = ([A] => F[E, A]) ~> E
  type GAlgebra[W[_[_], _], F[_[_], _], E[_]] = ([a] => F[[b] => W[E, b], a]) ~> E

  trait HFunctor[H[_[_], _]] {
    // def fmap[F[_]: Functor, A, B](hfa: H[F, A])(f: A => B): H[F, B]
    def hmap[F[_], G[_]](f: F ~> G): ([A] => H[F, A]) ~> ([A] => H[G, A])
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
    type in[A] = F[A]
    type out[A] = G[A]
    def apply[A](fa: in[A]): out[A]
  }


  implicit val hfixRecursive: HRecursive[HFix] =
    new HRecursive[HFix] {
      override def hproject[F[_[_], _], A](t: HFix[F, A]) = t.unfix
    }

  object HRecursive {
    def apply[T[_[_[_], _], _]](implicit r: HRecursive[T]): HRecursive[T] = r
  }

  trait HRecursive[T[_[_[_], _], _]] {
    type Out[G[_[_], _], I] = G[[B] => T[G, B], I]

    def hproject[F[_[_], _], A](t: T[F, A]): Out[F, A]
    def cata[F[_[_], _]: HFunctor, A[_]](φ: Algebra[F, A]): ([B] => T[F, B]) ~> A =
      new (([B] => T[F, B]) ~> A) {
        def apply[Q](t: T[F, Q]) =
          φ(HFunctor[F].hmap(cata(φ))(hproject(t)))
      }

    def para[F[_[_], _]: HFunctor, A[_]](
      φ: GAlgebra[[γ[_], α] => (T[F, α], γ[α]), F, A]):
    ([B] => T[F, B]) ~> A =
      new (([B] => T[F, B]) ~> A) {
        def apply[Q](t: T[F, Q]) =
          φ(HFunctor[F].hmap[[B] => T[F, B], [B] => (T[F, B], A[B])](
            new (([B] => T[F, B]) ~> ([B] => (T[F, B], A[B]))) {
              def apply[P](t: T[F, P]) = (t, para(φ).apply(t))
            })(hproject[F, Q](t)))
      }

  }

  implicit class HRecursiveOps[F[_[_[_], _], _], G[_[_], _], I](f: F[G, I]) {
    def hproject(implicit hrecursive: HRecursive[F]): hrecursive.Out[G, I] = hrecursive.hproject(f)
    def para[A[_]](alg: GAlgebra[[γ[_], α] => (F[G, α], γ[α]), G, A])(implicit hrecursive: HRecursive[F], hfunctor: HFunctor[G]): A[I] =
      hrecursive.para(alg).apply(f)
  }

  implicit def queryHFunctor[T]: HFunctor[[A[_], I] => Query[T, A, I]] = new HFunctor[[A[_], I] => Query[T, A, I]] {
    type Q[A[_], I] = Query[T, A, I] // Make a type alias for query with an Invariant Index. Dotty will explode if the Index is Covariant.
    def hmap[F[_], G[_]](f: F ~> G) = new ~>[[I] => Q[F, I], [I] => Q[G, I]] {
      def apply[I](fa: Q[F, I]): Q[G, I] =
        fa match {
          case Parameter(param) => Parameter[T, G](param)
          case Function(path, args) => Function(path, args.map(f[Indicies.Value]))
          case PathValue(path) => PathValue(path)
          case ValueBinOp(l, r, op) => ValueBinOp(f(l), f(r), op)
          case _: Null[_, [_] => _] => Null[T, G]

          case ComparisonBinOp(l, r, op) => ComparisonBinOp(f(l), f(r), op)
          case ComparisonValueBinOp(l, r, op) => ComparisonValueBinOp(f(l), f(r), op)
          case In(l, rs) => In(f(l), rs.map(f[Indicies.Value]))
          case Lit(v) => Lit(f(v))
          case Not(v) => Not(f(v))
          case _: ComparisonNop[_, [_] => _] => ComparisonNop[T, G]

          case ProjectOne(value) => ProjectOne(f(value))
          case ProjectAlias(value, alias) => ProjectAlias(f(value), alias)
          case _: ProjectAll[_, [_] => _] => ProjectAll[T, G]

          case QueryJoin(projection, on, op) => QueryJoin(f(projection), f(on), op)

          case ModifyField(path, value) => ModifyField(path, f(value))

          case QueryDelete(table, where) => QueryDelete(table, f(where))
          case QueryInsert(table, values) => QueryInsert(table, values.map(f[Indicies.ModifyFieldI]))
          case QueryUpdate(table, values, where) => QueryUpdate(table, values.map(f[Indicies.ModifyFieldI]), f(where))
          case QuerySelect(table, values, joins, filter, sorts, groupings, offset, limit) =>
            QuerySelect(
              f(table),
              values.map(f[Indicies.Projection]),
              joins.map(f[Indicies.Join]),
              f(filter),
              sorts,
              groupings,
              offset,
              limit
            )
        }
    }
  }

}

