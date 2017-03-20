package scoobie.doobie.doo

import doobie.imports._
import scoobie.ast._
import scoobie.doobie.{DoobieSupport, ScoobieFragmentProducer, SqlInterpreter}
import shapeless.HNil

/**
  * Created by jbarber on 5/20/16.
  */
object postgres extends DoobieSupport {

  implicit val interpreter = SqlInterpreter(interpretSql(_, "\""))

  def interpretSql(expr: QueryExpression[ScoobieFragmentProducer], escapeFieldWith: String): Fragment = {
    def wrap(s: Fragment, using: Fragment, usingRight: Option[Fragment] = None): Fragment = using ++ s ++ usingRight.getOrElse(using)

    def commas(frags: List[Fragment]): Fragment = frags.foldLeft(Option.empty[Fragment]){
      case (Some(acc), fr) => Some(acc ++ fr"," ++ fr)
      case (None, fr) => Some(fr)
    }.getOrElse(fr"")

    def spaces(fr: List[Fragment]): Fragment = fr.foldLeft(fr0"")((acc, fr) => acc ++ fr)

    def parens(fr: Fragment): Fragment = fr0"(" ++ fr ++ fr")"

    def parensAndCommas(frags: List[Fragment]): Fragment =
        parens(commas(frags))

    def binOpReduction[B](op: Fragment, left: B, right: B)(f: B => Fragment) = f(left) ++ wrap(op, fr0" ") ++ f(right)

    def reducePath(queryPath: QueryPath[ScoobieFragmentProducer], trailingSpace: Boolean = true): Fragment = queryPath match {
      case QueryPathEnd(str) => wrap(fragment(str), fragment(escapeFieldWith)) ++ (if (trailingSpace) fr0" " else fr0"")
      case QueryPathCons(head, tail) => wrap(fragment(head), fragment(escapeFieldWith)) ++ fr0"." ++ reducePath(tail, trailingSpace)
    }

    def reduceProjection(projection: QueryProjection[ScoobieFragmentProducer]): Fragment = projection match {
      case _: QueryProjectAll[ScoobieFragmentProducer] => fr0"*"
      case QueryProjectOne(path, alias) => reduceValue(path) ++ fragment(alias.map("AS " + _ + " ").getOrElse(""))
    }

    def reduceValue(value: QueryValue[ScoobieFragmentProducer]): Fragment = value match {
      case p @ QueryParameter(s) =>
        p.ev.genFragment(s)
      case QueryPathCons(a, b) => reducePath(QueryPathCons(a, b))
      case QueryPathEnd(a) => reducePath(QueryPathEnd(a))
      case QueryFunction(path, args) => reducePath(path, false) ++ parensAndCommas(args.map(reduceValue))
      case QueryAdd(left, right) => binOpReduction(fr"+", left, right)(reduceValue)
      case QuerySub(left, right) => binOpReduction(fr"-", left, right)(reduceValue)
      case QueryDiv(left, right) => binOpReduction(fr"/", left, right)(reduceValue)
      case QueryMul(left, right) => binOpReduction(fr"*", left, right)(reduceValue)
      case sel: QuerySelect[ScoobieFragmentProducer] => fr0"(" ++ interpretSql(sel, escapeFieldWith) ++ fr0")"
      case _: QueryNull[ScoobieFragmentProducer] => fr"NULL"
    }

    def reduceComparison(value: QueryComparison[ScoobieFragmentProducer]): Fragment = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, _: QueryNull[ScoobieFragmentProducer]) => reduceValue(left) ++ fr"IS NULL"
      case QueryEqual(_: QueryNull[ScoobieFragmentProducer], right) => reduceValue(right) ++ fr"IS NULL"
      case QueryEqual(left, right) => binOpReduction(fr"=", left, right)(reduceValue)
      case QueryNot(QueryEqual(left, _: QueryNull[ScoobieFragmentProducer])) => reduceValue(left) ++ fr"IS NOT NULL"
      case QueryNot(QueryEqual(_: QueryNull[ScoobieFragmentProducer], right)) => reduceValue(right) ++ fr"IS NOT NULL"
      case QueryNot(QueryEqual(left, right)) => binOpReduction(fr"<>", left, right)(reduceValue)
      case QueryGreaterThan(left, right) => binOpReduction(fr">", left, right)(reduceValue)
      case QueryGreaterThanOrEqual(left, right) => binOpReduction(fr">=", left, right)(reduceValue)
      case QueryIn(left, rights) => reduceValue(left) ++ fr" IN " ++ parensAndCommas(rights.map(reduceValue))
      case QueryLessThan(left, right) => binOpReduction(fr"<", left, right)(reduceValue)
      case QueryLessThanOrEqual(left, right) => binOpReduction(fr"<=", left, right)(reduceValue)
      case QueryAnd(_: QueryComparisonNop[ScoobieFragmentProducer], right) => reduceComparison(right)
      case QueryAnd(left , _: QueryComparisonNop[ScoobieFragmentProducer]) => reduceComparison(left)
      case QueryAnd(left, right) => binOpReduction(fr"AND", left, right)(reduceComparison)
      case QueryOr(_: QueryComparisonNop[ScoobieFragmentProducer], right) => reduceComparison(right)
      case QueryOr(left , _: QueryComparisonNop[ScoobieFragmentProducer]) => reduceComparison(left)
      case QueryOr(left, right) => binOpReduction(fr"OR", left, right)(reduceComparison)
      case QueryNot(v) => fr"NOT (" ++ reduceComparison(v) ++ fr")"
      case _: QueryComparisonNop[ScoobieFragmentProducer] => fr""
    }

    def reduceUnion(union: QueryUnion[ScoobieFragmentProducer]): Fragment = union match {
      case QueryLeftOuterJoin(path, logic) => fr"LEFT OUTER JOIN" ++ reduceProjection(path) ++ fr"ON" ++ reduceComparison(logic)
      case QueryRightOuterJoin(path, logic) => fr"RIGHT OUTER JOIN" ++ reduceProjection(path) ++ fr"ON" ++ reduceComparison(logic)
      case QueryCrossJoin(path, logic) => fr"CROSS JOIN" ++ reduceProjection(path) ++ fr"ON" ++ reduceComparison(logic)
      case QueryFullOuterJoin(path, logic) => fr"FULL OUTER JOIN" ++ reduceProjection(path) ++ fr"ON" ++ reduceComparison(logic)
      case QueryInnerJoin(path, logic) => fr"INNER JOIN" ++ reduceProjection(path) ++ fr"ON" ++ reduceComparison(logic)
    }

    def reduceSort(sort: QuerySort[ScoobieFragmentProducer]): Fragment = sort match {
      case QuerySortAsc(path) => reducePath(path) ++ fr" ASC"
      case QuerySortDesc(path) => reducePath(path) ++ fr" DESC"
    }

    def reduceInsertValues(insertValue: ModifyField[ScoobieFragmentProducer]): (Fragment, Fragment) =
      reducePath(insertValue.key) -> reduceValue(insertValue.value)

    expr match {
      case QuerySelect(table, values, unions, filters, sorts, groups, offset, limit) =>
        val sqlProjections = commas(values.map(reduceProjection))
        val sqlFilter = if(filters == QueryComparisonNop[ScoobieFragmentProducer]) fr0"" else fr"WHERE" ++ reduceComparison(filters)
        val sqlUnions = spaces(unions.map(reduceUnion))

        val sqlSorts =
          if (sorts.isEmpty) fr0""
          else fr"ORDER BY" ++ commas(sorts.map(reduceSort))

        val sqlGroups =
          if (groups.isEmpty) fr0""
          else fr"GROUP BY" ++ commas(groups.map(reduceSort))

        val sqlTable = reduceProjection(table)

        val sqlOffset = offset.map(n => fr"OFFSET" ++ fr"$n").getOrElse(fr0"")
        val sqlLimit = limit.map(n => fr"LIMIT" ++ fr"$n").getOrElse(fr0"")

        fr"SELECT" ++ sqlProjections ++ fr"FROM" ++ sqlTable ++ sqlUnions ++ sqlFilter ++ sqlSorts ++ sqlGroups ++ sqlLimit ++ sqlOffset

      case QueryInsert(table, values) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues)
        val sqlColumns = commas(mappedSqlValuesKV.map(_._1))
        val sqlValues = commas(mappedSqlValuesKV.map(_._2))

        fr"INSERT INTO" ++ sqlTable ++ parens(sqlColumns) ++ fr"VALUES" ++ parens(sqlValues)

      case QueryUpdate(table, values, where) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = commas(values.map(reduceInsertValues).map(kv => kv._1 ++ fr"=" ++ kv._2))
        val sqlWhere = if(where == QueryComparisonNop[ScoobieFragmentProducer]) fr" " else fr"WHERE " ++ reduceComparison(where)

        fr"UPDATE" ++ sqlTable ++ fr"SET" ++ mappedSqlValuesKV ++ sqlWhere

      case QueryDelete(table, where) =>
        val sqlTable = reducePath(table)
        val sqlWhere = reduceComparison(where)

        fr"DELETE FROM" ++ sqlTable ++ fr"WHERE" ++ sqlWhere
    }

  }

  def fragment(s: String): Fragment = new StringContext(s).fr0()
}
