package scoobie.doobie.doo

import scoobie.ast._
import scoobie.doobie.{DoobieSupport, SqlInterpreter}

/**
  * Created by jbarber on 5/20/16.
  */
object postgres extends DoobieSupport {

  implicit val interpreter = SqlInterpreter(interpretSql(_, "\""))

  def interpretSql(expr: QueryExpression[_], escapeFieldWith: String): String = {
    def wrap(s: String, using: String): String = s"$using$s$using"

    def binOpReduction[A](op: String, left: A, right: A)(f: A => String) = f(left) + wrap(op, " ") + f(right)

    def reducePath(queryPath: QueryPath): String = queryPath match {
      case QueryPathEnd(str) => wrap(str, escapeFieldWith)
      case QueryPathCons(head, tail) => wrap(head, escapeFieldWith) + "." + reducePath(tail)
    }

    def reduceProjection(projection: QueryProjection[_]): String = projection match {
      case QueryProjectAll => "*"
      case QueryProjectOne(path, alias) => reduceValue(path) + alias.map(" AS " + _).getOrElse("")
    }

    def reduceValue(value: QueryValue[_]): String = value match {
      case expr @ QueryRawExpression(ex) => expr.rawExpressionHandler.interpret(ex)
      case QueryParameter(s) => "?"
      case QueryPathCons(a, b) => reducePath(QueryPathCons(a, b))
      case QueryPathEnd(a) => reducePath(QueryPathEnd(a))
      case QueryFunction(path, args, _) => reducePath(path) + "(" + args.map(reduceValue).mkString(", ") + ")"
      case QueryAdd(left, right, _) => binOpReduction("+", left, right)(reduceValue)
      case QuerySub(left, right, _) => binOpReduction("-", left, right)(reduceValue)
      case QueryDiv(left, right, _) => binOpReduction("/", left, right)(reduceValue)
      case QueryMul(left, right, _) => binOpReduction("*", left, right)(reduceValue)
      case sel: QuerySelect[_] => "(" + interpretSql(sel, escapeFieldWith) + ")"
      case QueryNull => "NULL"
    }

    def reduceComparison(value: QueryComparison[_]): String = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, QueryNull, _) => reduceValue(left) + " IS NULL"
      case QueryEqual(QueryNull, right, _) => reduceValue(right) + " IS NULL"
      case QueryEqual(left, right, _) => binOpReduction("=", left, right)(reduceValue)
      case QueryNot(QueryEqual(left, QueryNull, _)) => reduceValue(left) + " IS NOT NULL"
      case QueryNot(QueryEqual(QueryNull, right, _)) => reduceValue(right) + " IS NOT NULL"
      case QueryNot(QueryEqual(left, right, _)) => binOpReduction("<>", left, right)(reduceValue)
      case QueryGreaterThan(left, right, _) => binOpReduction(">", left, right)(reduceValue)
      case QueryGreaterThanOrEqual(left, right, _) => binOpReduction(">=", left, right)(reduceValue)
      case QueryIn(left, rights, _) => reduceValue(left) + " IN " + rights.map(reduceValue).mkString("(", ", ", ")")
      case QueryLessThan(left, right, _) => binOpReduction("<", left, right)(reduceValue)
      case QueryLessThanOrEqual(left, right, _) => binOpReduction("<=", left, right)(reduceValue)
      case QueryAnd(_: QueryComparisonNop.type, right, _) => reduceComparison(right)
      case QueryAnd(left , _: QueryComparisonNop.type, _) => reduceComparison(left)
      case QueryAnd(left, right, _) => binOpReduction(" AND ", left, right)(reduceComparison)
      case QueryOr(_: QueryComparisonNop.type, right, _) => reduceComparison(right)
      case QueryOr(left , _: QueryComparisonNop.type, _) => reduceComparison(left)
      case QueryOr(left, right, _) => binOpReduction(" OR ", left, right)(reduceComparison)
      case QueryNot(v) => "NOT (" + reduceComparison(v) + ")"
      case QueryComparisonNop => ""
    }

    def reduceUnion(union: QueryUnion[_]): String = union match {
      case QueryLeftOuterJoin(path, logic, _) => "LEFT OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryRightOuterJoin(path, logic, _) => "RIGHT OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryCrossJoin(path, logic, _) => "CROSS JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryFullOuterJoin(path, logic, _) => "FULL OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryInnerJoin(path, logic, _) => "INNER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
    }

    def reduceSort(sort: QuerySort): String = sort match {
      case QuerySortAsc(path) => reducePath(path) + " ASC"
      case QuerySortDesc(path) => reducePath(path) + " DESC"
    }

    def reduceInsertValues(insertValue: ModifyField[_]): (String, String) =
      reducePath(insertValue.key) -> reduceValue(insertValue.value)

    expr match {
      case QuerySelect(table, values, unions, filters, sorts, groups, offset, limit, _) =>
        val sqlProjections = values.map(reduceProjection).mkString(", ")
        val sqlFilter = if(filters == QueryComparisonNop) " " else "WHERE " + reduceComparison(filters)
        val sqlUnions = unions.map(reduceUnion).mkString(" ")

        val sqlSorts =
          if (sorts.isEmpty) ""
          else "ORDER BY " + sorts.map(reduceSort).mkString(", ")

        val sqlGroups =
          if (groups.isEmpty) ""
          else "GROUP BY " + groups.map(reduceSort).mkString(", ")

        val sqlTable = reduceProjection(table)

        val sqlOffset = offset.map("OFFSET " + _).getOrElse("")
        val sqlLimit = limit.map("LIMIT " + _).getOrElse("")

        s"SELECT $sqlProjections FROM $sqlTable $sqlUnions $sqlFilter $sqlSorts $sqlGroups $sqlLimit $sqlOffset".trim

      case QueryInsert(table, values, _) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues)
        val sqlColumns = mappedSqlValuesKV.map(_._1).mkString(", ")
        val sqlValues = mappedSqlValuesKV.map(_._2).mkString(", ")

        s"INSERT INTO $sqlTable ($sqlColumns) VALUES ($sqlValues)"

      case QueryUpdate(table, values, where, _) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues).map(kv => kv._1 + "=" + kv._2).mkString(", ")
        val sqlWhere = if(where == QueryComparisonNop) " " else "WHERE " + reduceComparison(where)

        s"UPDATE $sqlTable SET $mappedSqlValuesKV $sqlWhere"

      case QueryDelete(table, where) =>
        val sqlTable = reducePath(table)
        val sqlWhere = reduceComparison(where)

        s"DELETE FROM $sqlTable WHERE $sqlWhere"
    }

  }
}
