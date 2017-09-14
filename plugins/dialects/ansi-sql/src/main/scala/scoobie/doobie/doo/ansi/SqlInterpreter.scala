package scoobie.doobie.doo.ansi

import SqlInterpreter._
import scoobie.ast._
import scoobie.cata._

object SqlInterpreter {
  case class LiteralQueryString(s: String) extends AnyVal

  trait Semigroup[A] {
    def append(l: A, r: A): A
  }

  implicit class SemigroupOps[A](l: A) {
    def |+|(r: A)(implicit semigroup: Semigroup[A]): A = semigroup.append(l, r)
  }
}

/**
  * Generates sql query strings, converting them in to type B using a typeclass F[_].
  *
  * @param pathWrapper
  * @param lifter Something that knows how to use the typeclass F to produce values of B.
  * @param litSqlInterpreter An implementation of F[_] for handling longs.
  * @tparam B A type we can produce using an value for F[A] and a value for A. B must be semigroupal.
  */
case class SqlInterpreter[T: Semigroup](pathWrapper: String, litSqlInterpreter: (String => T)) {

  type ANSIAST[A[_], I] = Query[T, A, I]
  type Const[I] = T

  implicit class SqlLitInterpolator(val s: StringContext) {
    def litSql(params: String*): T = evaluatedLitSql(s.standardInterpolator(identity, params))
  }

  def evaluatedLitSql(s: String): T = litSqlInterpreter(s)

  def binOpReduction[U](op: T, left: U, right: U)(f: U => T) = f(left) |+| wrap(op, litSql" ") |+| f(right)
  def wrap(s: T, using: T, usingRight: Option[T] = None): T = using |+| s |+| usingRight.getOrElse(using)

  def reducePath(queryPath: Path, trailingSpace: Boolean = true): T = queryPath match {
    case PathEnd(str) => wrap(evaluatedLitSql(str), evaluatedLitSql(pathWrapper)) |+| (if (trailingSpace) litSql" " else litSql"")
    case PathCons(head, tail) => wrap(evaluatedLitSql(head), evaluatedLitSql(pathWrapper)) |+| litSql"." |+| reducePath(tail, trailingSpace)
  }

  import SortType._
  def reduceSort(sort: Sort): T = sort match {
    case Sort(path, Ascending) => reducePath(path) |+| litSql" ASC "
    case Sort(path, Descending) => reducePath(path) |+| litSql" DESC "
  }

  def commas(ts: List[T]): T = ts.foldLeft(Option.empty[T]){
    case (Some(acc), fr) => Some(acc |+| litSql", " |+| fr)
    case (None, fr) => Some(fr)
  }.getOrElse(litSql" ")

  def spaces(ts: List[T]): T = ts.foldLeft(litSql"")((acc, fr) => acc |+| fr)

  def parens(t: T): T = litSql"(" |+| t |+| litSql") "

  def parensAndCommas(frags: List[T]): T =
    parens(commas(frags))

  def interpreterAlgebra[F[_[_[_], _], _]]
                    (implicit queryFunctor: HFunctor[ANSIAST],
                              hRecursive: HRecursive[F]): GAlgebra[[G[_], X] => (F[ANSIAST, X], G[X]), ANSIAST, Const] = {

    type AST[X] = ANSIAST[[Y] => F[ANSIAST, Y], X]


    object HEmbed {
      def unapply[X](f: F[ANSIAST, X]) = Some(f.hproject)
    }

    def alg: GAlgebra[[G[_], X] => (F[ANSIAST, X], G[X]), ANSIAST, Const] =
      new GAlgebra[[G[_], X] => (F[ANSIAST, X], G[X]), ANSIAST, Const] {
        def apply[I](ast: ANSIAST[[X] => (ANSIAST[[Y] => F[ANSIAST, Y], X], Const[X]), I]): Const[I] = {
          import ValueOperators._
          import ComparisonOperators._
          import ComparisonValueOperators._
          import JoinOperators._
          ast match {
            case Parameter(param) => param
            case Function(path, args) => reducePath(path) |+| parensAndCommas(args.map(_._2))
            case PathValue(path) => reducePath(path)
            case ValueBinOp((_, left), (_, right), Add) => left |+| litSql" + " |+| right
            case ValueBinOp((_, left), (_, right), Subtract) => left |+| litSql" - " |+| right
            case ValueBinOp((_, left), (_, right), Multiply) => left |+| litSql" * " |+| right
            case ValueBinOp((_, left), (_, right), Divide) => left |+| litSql" / " |+| right
            case _: Null[T, in] => litSql"NULL"
            case Lit((_, v)) => v
            case ComparisonValueBinOp((_, left), (_: Null[_, in], _), Equal) => left |+| litSql"IS NULL "
            case ComparisonValueBinOp((_: Null[_, in], _), (_, right), Equal) => right |+| litSql"IS NULL "
            case ComparisonValueBinOp((_, left), (_, right), Equal) => left |+| litSql" = " |+| right
            case ComparisonValueBinOp((_, left), (_, right), LessThan) => left |+| litSql" < " |+| right
            case ComparisonValueBinOp((_, left), (_, right), LessThanOrEqual) => left |+| litSql" <= " |+| right
            case ComparisonValueBinOp((_, left), (_, right), GreaterThan) => left |+| litSql" > " |+| right
            case ComparisonValueBinOp((_, left), (_, right), GreaterThanOrEqual) => left |+| litSql" >= " |+| right
            case Not((ComparisonValueBinOp(HEmbed(_: Null[_, in]), right, Equal), _)) => hRecursive.para[ANSIAST, Const](alg).apply(right) |+| litSql"IS NOT NULL "
            case Not((ComparisonValueBinOp(left, HEmbed(_: Null[_, in]), Equal), _)) => hRecursive.para[ANSIAST, Const](alg).apply(left) |+| litSql"IS NOT NULL "
            case Not((_, v)) => litSql"NOT " |+| parens(v)
            case In((_, left), rights) => left |+| litSql" IN  " |+| parensAndCommas(rights.map(_._2))
            case ComparisonBinOp((_, left), (_, right), And) => left |+| litSql" AND " |+| right
            case ComparisonBinOp((_, left), (_, right), Or) => left |+| litSql" AND " |+| right
            case ComparisonBinOp((_: ComparisonNop[_, in], _), (_, right), Or) => right
            case ComparisonBinOp((_: ComparisonNop[_, in], _), (_, right), And) => right
            case ComparisonBinOp((_, left), (_: ComparisonNop[_, in], _), And) => left
            case ComparisonBinOp((_, left), (_: ComparisonNop[_, in], _), Or) => left
            case _: ComparisonNop[_, in] => litSql" "

            case ProjectOne((_, value)) => parens(value)
            case ProjectAlias((_, value), alias) => value |+| litSql"AS " |+| evaluatedLitSql(alias)
            case _: ProjectAll[_, in] => litSql"*"

            case QueryJoin((_, prj), (_, where), FullOuter)  => litSql"FULL OUTER JOIN " |+| prj |+| litSql" ON " |+| where
            case QueryJoin((_, prj), (_, where), LeftOuter)  => litSql"LEFT OUTER JOIN " |+| prj |+| litSql" ON " |+| where
            case QueryJoin((_, prj), (_, where), RightOuter) => litSql"RIGHT OUTER JOIN " |+| prj |+| litSql" ON " |+| where
            case QueryJoin((_, prj), (_, where), Cartesian)  => litSql"CROSS JOIN " |+| prj |+| litSql" ON " |+| where
            case QueryJoin((_, prj), (_, where), Inner)      => litSql"INNER JOIN " |+| prj |+| litSql" ON " |+| where

            case ModifyField(path, (_, value)) => reducePath(path) |+| litSql"=" |+| value

            case QueryDelete(table, (_, where)) =>
              litSql"DELETE FROM " |+| reducePath(table) |+| litSql" WHERE " |+| where

            case QueryDelete(table, (_: ComparisonNop[_, in], _)) =>
              litSql"DELETE FROM " |+| reducePath(table)

            case QueryInsert(table, values) =>
              litSql"INSERT INTO " |+| reducePath(table) |+| litSql" SET " |+| commas(values.map(_._2))

            case QueryUpdate(table, values, (_, where)) =>
              litSql"UPDATE " |+| reducePath(table) |+| litSql" SET " |+| commas(values.map(_._2)) |+| litSql" WHERE " |+| where

            case QueryUpdate(table, values, (_: ComparisonNop[_, in], _)) =>
              litSql"UPDATE " |+| reducePath(table) |+| litSql" SET " |+| commas(values.map(_._2))

            case QuerySelect((_, table), values, joins, (_, filters), sorts, groups, offset, limit) =>
              val sqlProjections = commas(values.map(_._2))
              val sqlFilter = if(filters == ComparisonNop) litSql"" else litSql"WHERE " |+| filters
              val sqlJoins = spaces(joins.map(_._2))

              val sqlSorts =
                if (sorts.isEmpty) litSql""
                else litSql"ORDER BY " |+| commas(sorts.map(reduceSort))

              val sqlGroups =
                if (groups.isEmpty) litSql""
                else litSql"GROUP BY " |+| commas(groups.map(reduceSort))

              val sqlOffset = offset.map(n => litSql"OFFSET " |+| n |+| litSql" ").getOrElse(litSql"")
              val sqlLimit = limit.map(n => litSql"LIMIT " |+| n |+| litSql" ").getOrElse(litSql"")

              litSql"SELECT " |+| sqlProjections |+| litSql"FROM " |+| table |+| sqlJoins |+| sqlFilter |+| sqlSorts |+| sqlGroups |+| sqlLimit |+| sqlOffset
          }
        }
      }
    alg
  }
}
