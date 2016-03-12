package com.github.jacoby6000.query

import com.github.jacoby6000.query.ast._

/**
  * Created by jacob.barber on 3/3/16.
  */
object interpreter {
  def interpretPSql(expr: Expression): String = {
    val singleQuote = '"'.toString
    def wrap(s: String, using: String): String = s"$using$s$using"

    def binOpReduction[A](op: String, left: A, right: A)(f: A => String) = f(left) + wrap(op, " ") + f(right)

    def reducePath(queryPath: QueryPath): String = queryPath match {
      case QueryPathEnd(str) => wrap(str, singleQuote)
      case QueryPathCons(head, tail) => wrap(head, singleQuote) + "." + reducePath(tail)
    }

    def reduceProjection(projection: QueryProjection): String = projection match {
      case QueryProjectAll => "*"
      case QueryProjectOne(path, alias) => reduceValue(path) + alias.map(" AS " + _).getOrElse("")
    }

    def reduceValue(value: QueryValue): String = value match {
      case QueryRawExpression(ex) => ex.toString
      case QueryString(s) => "'" + s + "'"
      case QueryInt(n) => n.toString
      case QueryDouble(d) => d.toString
      case QueryBoolean(b) => b.toString
      case QueryPathCons(a,b) => reducePath(QueryPathCons(a,b))
      case QueryPathEnd(a) => reducePath(QueryPathEnd(a))
      case QueryFunction(path, args) => reducePath(path) + "(" + args.map(reduceValue).mkString(", ") + ")"
      case QueryAdd(left, right) => binOpReduction("+", left, right)(reduceValue)
      case QuerySub(left, right) => binOpReduction("-", left, right)(reduceValue)
      case QueryDiv(left, right) => binOpReduction("/", left, right)(reduceValue)
      case QueryMul(left, right) => binOpReduction("*", left, right)(reduceValue)
      case sel: QuerySelect => "(" + interpretPSql(sel) + ")"
      case QueryParameter => "?"
      case QueryNull => "NULL"
    }

    def reduceComparison(value: QueryComparison): String = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, QueryNull) => reduceValue(left) + " IS NULL"
      case QueryEqual(left, right) => binOpReduction("=", left, right)(reduceValue)
      case QueryNotEqual(left, QueryNull) => reduceValue(left) + " IS NOT NULL"
      case QueryNotEqual(left, right) => binOpReduction("<>", left, right)(reduceValue)
      case QueryGreaterThan(left, right) => binOpReduction(">", left, right)(reduceValue)
      case QueryGreaterThanOrEqual(left, right) => binOpReduction(">=", left, right)(reduceValue)
      case QueryLessThan(left, right) => binOpReduction("<", left, right)(reduceValue)
      case QueryLessThanOrEqual(left, right) => binOpReduction("<=", left, right)(reduceValue)
      case QueryAnd(left, right) => binOpReduction(" AND ", left, right)(reduceComparison)
      case QueryOr(left, right) => binOpReduction(" OR ", left, right)(reduceComparison)
      case QueryNot(v) => "!" + reduceComparison(v)
    }

    def reduceUnion(union: QueryUnion): String = union match {
      case QueryLeftOuterJoin(path, logic) => "LEFT OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryRightOuterJoin(path, logic) => "RIGHT OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryCrossJoin(path, logic) => "CROSS JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryFullOuterJoin(path, logic) => "FULL OUTER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
      case QueryInnerJoin(path, logic) => "INNER JOIN " + reduceProjection(path) + " ON " + reduceComparison(logic)
    }

    def reduceSort(sort: QuerySort): String = sort match {
      case QuerySortAsc(path) => reducePath(path) + " ASC"
      case QuerySortDesc(path) => reducePath(path) + " DESC"
    }

    def reduceInsertValues(insertValue: ModifyField): (String, String) =
      reducePath(insertValue.key) -> reduceValue(insertValue.value)

    expr match {
      case QuerySelect(table, values, unions, filters, sorts, groups, offset, limit) =>
        val sqlProjections = values.map(reduceProjection).mkString(", ")
        val sqlFilter = filters.map("WHERE " + reduceComparison(_)).getOrElse("")
        val sqlUnions = unions.map(reduceUnion).mkString(" ")
        val sqlSorts =
          if(sorts.isEmpty) ""
          else "ORDER BY " + sorts.map(reduceSort).mkString(", ")

        val sqlGroups =
          if(groups.isEmpty) ""
          else "GROUP BY " + groups.map(reduceSort).mkString(", ")

        val sqlTable = reduceProjection(table)

        val sqlOffset = offset.map("OFFSET " + _).getOrElse("")
        val sqlLimit = limit.map("LIMIT " + _).getOrElse("")

        s"SELECT $sqlProjections FROM $sqlTable $sqlUnions $sqlFilter $sqlSorts $sqlGroups $sqlLimit $sqlOffset".trim

      case QueryInsert(table, values) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues)
        val sqlColumns = mappedSqlValuesKV.map(_._1).mkString(", ")
        val sqlValues = mappedSqlValuesKV.map(_._2).mkString(", ")

        s"INSERT INTO $sqlTable ($sqlColumns) VALUES ($sqlValues)"

      case QueryUpdate(table, values, where) =>
        val sqlTable = reducePath(table)
        val mappedSqlValuesKV = values.map(reduceInsertValues).map(kv => kv._1 + "=" + kv._2).mkString(", ")
        val sqlWhere = where.map("WHERE " + reduceComparison(_)).getOrElse("")

        s"UPDATE $sqlTable SET $mappedSqlValuesKV $sqlWhere"

      case QueryDelete(table, where) =>
        val sqlTable = reducePath(table)
        val sqlWhere = reduceComparison(where)

        s"DELETE $sqlTable WHERE $sqlWhere"
    }



  }
}
