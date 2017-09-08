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
                              hRecursive: HRecursive[F]): GAlgebra[[G[_], X] => (ANSIAST[G, X], F[ANSIAST, X]), ANSIAST, Const] = {

    type AST[X] = ANSIAST[[Y] => F[ANSIAST, Y], X]

    
    object HEmbed {
      def unapply[X](f: F[ANSIAST, X]) = Some(f.hproject)
    }

    def alg: GAlgebra[[G[_], X] => (ANSIAST[G, X], F[ANSIAST, X]), ANSIAST, Const] = 
      new GAlgebra[[G[_], X] => (ANSIAST[G, X], F[ANSIAST, X]), ANSIAST, Const] {
        def apply[I](ast: ANSIAST[[X] => (Const[X], ANSIAST[[Y] => F[ANSIAST, Y], X]), I]): Const[I] = {
          import ValueOperators._
          import ComparisonValueOperators._
          ast match {
            case Parameter(param) => param
            case Function(path, args) => reducePath(path) |+| parensAndCommas(args.map(_._1))
            case PathValue(path) => reducePath(path)
            case ValueBinOp((left, _), (right, _), Add) => binOpReduction(litSql"+ ", left, right)(identity)
            case ValueBinOp((left, _), (right, _), Subtract) => binOpReduction(litSql"- ", left, right)(identity)
            case ValueBinOp((left, _), (right, _), Multiply) => binOpReduction(litSql"* ", left, right)(identity)
            case ValueBinOp((left, _), (right, _), Divide) => binOpReduction(litSql"/ ", left, right)(identity)
            case _: Null[T, in] => litSql"NULL"
            case Lit((v, _)) => v
            case ComparisonValueBinOp((left, _), (_, _: Null[_, _]), Equal) => left |+| litSql"IS NULL "
            case ComparisonValueBinOp((_, _: Null[_, _]), (right, _), Equal) => right |+| litSql"IS NULL "
            case ComparisonValueBinOp((left, _), (right, _), Equal) => binOpReduction(litSql"= ", left, right)(identity)
            case Not((_, ComparisonValueBinOp(left, HEmbed(_: Null[_, _]), Equal))) => alg.apply(hRecursive.hproject(left)) /* |+| litSql"IS NOT NULL "
            case Not((_, ComparisonValueBinOp(HEmbed(_: Null[_, _]), right, Equal))) => right |+| litSql"IS NOT NULL "
            case Not((_, QueryEqual(left, right, Equal))) => binOpReduction(litSql"<> ", left, right)(identity)
            case In(left, rights) => reduceValue(left) |+| litSql" IN  " |+| parensAndCommas(rights.map(reduceValue))
            case Not(In(left, rights)) => reduceValue(left) |+| litSql" NOT IN  " |+| parensAndCommas(rights.map(reduceValue))
            case QueryLessThan(left, right) => binOpReduction(litSql"< ", left, right)(reduceValue)
            case QueryLessThanOrEqual(left, right) => binOpReduction(litSql"<= ", left, right)(reduceValue)
            case QueryAnd(_: QueryComparisonNop[F], right) => reduceComparison(right)
            case QueryAnd(left , _: QueryComparisonNop[F]) => reduceComparison(left)
            case QueryAnd(left, right) => binOpReduction(litSql"AND ", left, right)(reduceComparison)
            case QueryOr(_: QueryComparisonNop[F], right) => reduceComparison(right)
            case QueryOr(left , _: QueryComparisonNop[F]) => reduceComparison(left)
            case QueryOr(left, right) => binOpReduction(litSql"OR ", left, right)(reduceComparison)
            case Not(v) => litSql"NOT ( " |+| reduceComparison(v) |+| litSql") "
            case _: QueryComparisonNop[F] => litSql" "

            case ProjectOne(value) => ProjectOne(f(value))
            case ProjectAlias(value, alias) => ProjectAlias(f(value), alias)
            case _: ProjectAll[_, _] => ProjectAll[T, G]

            case QueryJoin(projection, on, op) => QueryJoin(f(projection), f(on), op)

            case ModifyField(path, value) => ModifyField(path, f(value))

            case QueryDelete(table, where) => QueryDelete(table, f(where))
            case Insert(table, values) => Insert(table, values.map(f[Indicies.ModifyFieldI]))
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
              ) */
          }
        }
      }
    alg




  }
}

/*
  def interpretSql(expr: QueryExpression): B = {




    def reduceValue(value: QueryValue[F]): B = value match {
      case p @ QueryParameter(s) =>
        lifter.liftValue(s, p.ev)
      case expr @ QueryRawExpression(v) =>
        evaluatedLitSql(expr.rawExpressionHandler.interpret(v))
      case QueryPathCons(a, b) => reducePath(QueryPathCons(a, b))
      case QueryPathEnd(a) => reducePath(QueryPathEnd(a))
      case QueryFunction(path, args) => reducePath(path, false) |+| parensAndCommas(args.map(reduceValue))
      case QueryAdd(left, right) => binOpReduction(litSql"+ ", left, right)(reduceValue)
      case QuerySub(left, right) => binOpReduction(litSql"- ", left, right)(reduceValue)
      case QueryDiv(left, right) => binOpReduction(litSql"/ ", left, right)(reduceValue)
      case QueryMul(left, right) => binOpReduction(litSql"* ", left, right)(reduceValue)
      case sel: QuerySelect[F] => litSql"(" |+| interpretSql(sel) |+| litSql")"
      case _: Null[F] => litSql"NULL "
    }

    def reduceComparison(value: QueryComparison[F]): B = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, _: Null[F]) => reduceValue(left) |+| litSql"IS NULL "
      case QueryEqual(_: Null[F], right) => reduceValue(right) |+| litSql"IS NULL "
      case QueryEqual(left, right) => binOpReduction(litSql"= ", left, right)(reduceValue)
      case Not(QueryEqual(left, _: Null[F])) => reduceValue(left) |+| litSql"IS NOT NULL "
      case Not(QueryEqual(_: Null[F], right)) => reduceValue(right) |+| litSql"IS NOT NULL "
      case Not(QueryEqual(left, right)) => binOpReduction(litSql"<> ", left, right)(reduceValue)
      case QueryGreaterThan(left, right) => binOpReduction(litSql"> ", left, right)(reduceValue)
      case QueryGreaterThanOrEqual(left, right) => binOpReduction(litSql">= ", left, right)(reduceValue)
      case In(left, rights) => reduceValue(left) |+| litSql" IN  " |+| parensAndCommas(rights.map(reduceValue))
      case Not(In(left, rights)) => reduceValue(left) |+| litSql" NOT IN  " |+| parensAndCommas(rights.map(reduceValue))
      case QueryLessThan(left, right) => binOpReduction(litSql"< ", left, right)(reduceValue)
      case QueryLessThanOrEqual(left, right) => binOpReduction(litSql"<= ", left, right)(reduceValue)
      case QueryAnd(_: QueryComparisonNop[F], right) => reduceComparison(right)
      case QueryAnd(left , _: QueryComparisonNop[F]) => reduceComparison(left)
      case QueryAnd(left, right) => binOpReduction(litSql"AND ", left, right)(reduceComparison)
      case QueryOr(_: QueryComparisonNop[F], right) => reduceComparison(right)
      case QueryOr(left , _: QueryComparisonNop[F]) => reduceComparison(left)
      case QueryOr(left, right) => binOpReduction(litSql"OR ", left, right)(reduceComparison)
      case Not(v) => litSql"NOT ( " |+| reduceComparison(v) |+| litSql") "
      case _: QueryComparisonNop[F] => litSql" "
    }

    def reduceJoin(union: QueryJoin[F]): B = union match {
      case QueryLeftOuterJoin(path, logic) => litSql"LEFT OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryRightOuterJoin(path, logic) => litSql"RIGHT OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryCrossJoin(path, logic) => litSql"CROSS JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryFullOuterJoin(path, logic) => litSql"FULL OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case InnerJoin(path, logic) => litSql"INNER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
    }

    def reduceSort(sort: QuerySort[F]): B = sort match {
      case QuerySortAsc(path) => reducePath(path) |+| litSql" ASC "
      case QuerySortDesc(path) => reducePath(path) |+| litSql" DESC "
    }

    def reduceInsertValues(insertValue: ModifyField[F]): (B, B) =
      reducePath(insertValue.key) -> reduceValue(insertValue.value)

    expr match {
      case QuerySelect(table, values, unions, filters, sorts, groups, offset, limit) =>
        val sqlProjections = commas(values.map(reduceProjection))
        val sqlFilter = if(filters == QueryComparisonNop[F]) litSql"" else litSql"WHERE " |+| reduceComparison(filters)
        val sqlJoins = spaces(unions.map(reduceJoin))

        val sqlSorts =
          if (sorts.isEmpty) litSql""
          else litSql"ORDER BY " |+| commas(sorts.map(reduceSort))

        val sqlGroups =
          if (groups.isEmpty) litSql""
          else litSql"GROUP BY " |+| commas(groups.map(reduceSort))

        val sqlTable = reduceProjection(table)

        val sqlOffset = offset.map(n => litSql"OFFSET " |+| lifter.liftValue(n, longInterpreter) |+| litSql" ").getOrElse(litSql"")
        val sqlLimit = limit.map(n => litSql"LIMIT " |+| lifter.liftValue(n, longInterpreter) |+| litSql" ").getOrElse(litSql"")

        litSql"SELECT " |+| sqlProjections |+| litSql"FROM " |+| sqlTable |+| sqlJoins |+| sqlFilter |+| sqlSorts |+| sqlGroups |+| sqlLimit |+| sqlOffset

      case Insert(table, values) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues)
        val sqlColumns = commas(mappedSqlValuesKV.map(_._1))
        val sqlValues = commas(mappedSqlValuesKV.map(_._2))

        litSql"INSERT INTO " |+| sqlTable |+| parens(sqlColumns) |+| litSql"VALUES " |+| parens(sqlValues)

      case QueryUpdate(table, values, where) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = commas(values.map(reduceInsertValues).map(kv => kv._1 |+| litSql"= " |+| kv._2))
        val sqlWhere = if(where == QueryComparisonNop[F]) litSql"  " else litSql"WHERE  " |+| reduceComparison(where)

        litSql"UPDATE " |+| sqlTable |+| litSql"SET " |+| mappedSqlValuesKV |+| sqlWhere

      case QueryDelete(table, where) =>
        val sqlTable = reducePath(table)
        val sqlWhere = reduceComparison(where)

        litSql"DELETE FROM " |+| sqlTable |+| litSql"WHERE " |+| sqlWhere
    }

  }
}*/
