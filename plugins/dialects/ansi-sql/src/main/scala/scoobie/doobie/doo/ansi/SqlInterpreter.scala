package scoobie.doobie.doo.ansi

import scalaz._
import Scalaz._
import SqlInterpreter._
import scoobie.ast._
import scoobie.cata._

object SqlInterpreter {
  case class LiteralQueryString(s: String) extends AnyVal
}

/**
  * Generates sql query strings, converting them in to type B using a typeclass F[_].
  *
  * @param escapeFieldWith
  * @param lifter Something that knows how to use the typeclass F to produce values of B.
  * @param litSqlInterpreter An implementation of F[_] for handling longs.
  * @tparam B A type we can produce using an value for F[A] and a value for A. B must be semigroupal.
  */
case class SqlInterpreter[T: Semigroup](litSqlInterpreter: (String => T)) {

  type ANSIAST[A[_], I] = QueryAST[T]#of[A, I]

  implicit class SqlLitInterpolator(val s: StringContext) {
    def litSql(params: String*): T = s.standardInterpolator(evaluatedLitSql(params))
  }

  def evaluatedLitSql(s: String): T = litSqlInterpreter(LiteralQueryString(s), sqlFragmentInterpreter)

  def binOpReduction[A](op: B, left: A, right: A)(f: A => B) = f(left) |+| wrap(op, litSql" ") |+| f(right)
  def wrap(s: B, using: B, usingRight: Option[B] = None): B = using |+| s |+| usingRight.getOrElse(using)

  def interpreterAlgebra[I, F[_[_[_], _], _], M[_]]
                    (query: F[ANSIAST, I])
                    (implicit queryFunctor: HFunctor[ANSIAST],
                              hRecursive: HRecursive[F],
                              monad: Monad[M]): F[ANSIAST, ?] ~> M =
    query.cata {
      new (Algebra[ANSIAST, M]){
        def apply(ast: ANSIAST[M]): M = {
          ast match {
          case Parameter(param) => param.lift[M]
          case Function(path, args) => Function(path, args.map(f[Indicies.Value]))
          case PathValue(path) => PathValue(path)
          case ValueBinOp(l, r, op) => ValueBinOp(f(l), f(r), op)
          case _: Null[_, _] => Null[T, G]

          case ComparisonBinOp(l, r, op) => ComparisonBinOp(f(l), f(r), op)
          case ComparisonValueBinOp(l, r, op) => ComparisonValueBinOp(f(l), f(r), op)
          case In(l, rs) => In(f(l), rs.map(f[Indicies.Value]))
          case Lit(v) => Lit(f(v))
          case Not(v) => Not(f(v))
          case _: ComparisonNop[_, _] => ComparisonNop[T, G]

          case ProjectOne(value) => ProjectOne(f(value))
          case ProjectAlias(value, alias) => ProjectAlias(f(value), alias)
          case _: ProjectAll[_, _] => ProjectAll[T, G]

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
}

/*
  def interpretSql(expr: QueryExpression): B = {

    def commas(bs: List[B]): B = bs.foldLeft(Option.empty[B]){
      case (Some(acc), fr) => Some(acc |+| litSql", " |+| fr)
      case (None, fr) => Some(fr)
    }.getOrElse(litSql" ")

    def spaces(bs: List[B]): B = bs.foldLeft(litSql"")((acc, fr) => acc |+| fr)

    def parens(b: B): B = litSql"(" |+| b |+| litSql") "

    def parensAndCommas(frags: List[B]): B =
      parens(commas(frags))


    def reducePath(queryPath: QueryPath[F], trailingSpace: Boolean = true): B = queryPath match {
      case QueryPathEnd(str) => wrap(evaluatedLitSql(str), evaluatedLitSql(escapeFieldWith)) |+| (if (trailingSpace) litSql" " else litSql"")
      case QueryPathCons(head, tail) => wrap(evaluatedLitSql(head), evaluatedLitSql(escapeFieldWith)) |+| litSql"." |+| reducePath(tail, trailingSpace)
    }

    def reduceProjection(projection: QueryProjection[F]): B = projection match {
      case _: QueryProjectAll[F] => litSql"*"
      case QueryProjectOne(path, alias) => reduceValue(path) |+| evaluatedLitSql(alias.map("AS " + _ + " ").getOrElse(""))
    }

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
      case _: QueryNull[F] => litSql"NULL "
    }

    def reduceComparison(value: QueryComparison[F]): B = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, _: QueryNull[F]) => reduceValue(left) |+| litSql"IS NULL "
      case QueryEqual(_: QueryNull[F], right) => reduceValue(right) |+| litSql"IS NULL "
      case QueryEqual(left, right) => binOpReduction(litSql"= ", left, right)(reduceValue)
      case QueryNot(QueryEqual(left, _: QueryNull[F])) => reduceValue(left) |+| litSql"IS NOT NULL "
      case QueryNot(QueryEqual(_: QueryNull[F], right)) => reduceValue(right) |+| litSql"IS NOT NULL "
      case QueryNot(QueryEqual(left, right)) => binOpReduction(litSql"<> ", left, right)(reduceValue)
      case QueryGreaterThan(left, right) => binOpReduction(litSql"> ", left, right)(reduceValue)
      case QueryGreaterThanOrEqual(left, right) => binOpReduction(litSql">= ", left, right)(reduceValue)
      case QueryIn(left, rights) => reduceValue(left) |+| litSql" IN  " |+| parensAndCommas(rights.map(reduceValue))
      case QueryNot(QueryIn(left, rights)) => reduceValue(left) |+| litSql" NOT IN  " |+| parensAndCommas(rights.map(reduceValue))
      case QueryLessThan(left, right) => binOpReduction(litSql"< ", left, right)(reduceValue)
      case QueryLessThanOrEqual(left, right) => binOpReduction(litSql"<= ", left, right)(reduceValue)
      case QueryAnd(_: QueryComparisonNop[F], right) => reduceComparison(right)
      case QueryAnd(left , _: QueryComparisonNop[F]) => reduceComparison(left)
      case QueryAnd(left, right) => binOpReduction(litSql"AND ", left, right)(reduceComparison)
      case QueryOr(_: QueryComparisonNop[F], right) => reduceComparison(right)
      case QueryOr(left , _: QueryComparisonNop[F]) => reduceComparison(left)
      case QueryOr(left, right) => binOpReduction(litSql"OR ", left, right)(reduceComparison)
      case QueryNot(v) => litSql"NOT ( " |+| reduceComparison(v) |+| litSql") "
      case _: QueryComparisonNop[F] => litSql" "
    }

    def reduceJoin(union: QueryJoin[F]): B = union match {
      case QueryLeftOuterJoin(path, logic) => litSql"LEFT OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryRightOuterJoin(path, logic) => litSql"RIGHT OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryCrossJoin(path, logic) => litSql"CROSS JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryFullOuterJoin(path, logic) => litSql"FULL OUTER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
      case QueryInnerJoin(path, logic) => litSql"INNER JOIN " |+| reduceProjection(path) |+| litSql"ON " |+| reduceComparison(logic)
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

      case QueryInsert(table, values) =>
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
