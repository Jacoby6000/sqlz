package com.github.jacoby6000.query

import com.github.jacoby6000.query.ast._

/**
  * Created by jacob.barber on 3/3/16.
  */
object interpreter {
  def interpretSql(query: Query): String = {

    def reducePath(queryPath: QueryPath): String = queryPath match {
      case QueryPathEnd(str) => str
      case QueryPathCons(head, tail) => head + "." + reducePath(tail)
    }

    def reduceProjection(projection: QueryProjection): String = projection match {
      case QueryProjectAll => "*"
      case QueryProjectOne(path, alias) => reducePath(path) + alias.map(" AS " + _).getOrElse("")
    }

    def reduceValue(value: QueryValue): String = value match {
      case QueryString(s) => "\"" + s + "\""
      case QueryInt(n) => n.toString
      case QueryDouble(d) => d.toString
      case QueryBoolean(b) => b.toString
      case QueryPathCons(a,b) => reducePath(QueryPathCons(a,b))
      case QueryPathEnd(a) => reducePath(QueryPathEnd(a))
      case QueryFunction(path, args) => reducePath(path) + "(" + args.map(reduceValue).mkString(", ") + ")"
      case QueryAdd(left, right) => reduceValue(left) + "+" + reduceValue(right)
      case QuerySub(left, right) => reduceValue(left) + "-" + reduceValue(right)
      case QueryDiv(left, right) => reduceValue(left) + "/" + reduceValue(right)
      case QueryMul(left, right) => reduceValue(left) + "*" + reduceValue(right)
      case QueryParameter => "?"
    }

    def reduceComparison(value: QueryComparison): String = value match {
      case QueryLit(v) => reduceValue(v)
      case QueryEqual(left, right) => reduceValue(left) + " = " + reduceValue(right)
      case QueryNotEqual(left, right) => reduceValue(left) + " <> " + reduceValue(right)
      case QueryGreaterThan(left, right) => reduceValue(left) + " > " + reduceValue(right)
      case QueryGreaterThanOrEqual(left, right) => reduceValue(left) + " >= " + reduceValue(right)
      case QueryLessThan(left, right) => reduceValue(left) + " < " + reduceValue(right)
      case QueryLessThanOrEqual(left, right) => reduceValue(left) + " > " + reduceValue(right)
      case QueryAnd(left, right) => reduceComparison(left) + " AND " + reduceComparison(right)
      case QueryOr(left, right) => reduceComparison(left) + " OR " + reduceComparison(right)
      case QueryNot(v) => "!" + reduceComparison(v)
    }

    def reduceUnion(union: QueryUnion): String = union match {
      case QueryLeftOuterJoin(path, logic) => "LEFT OUTER JOIN " + reducePath(path) + " ON " + reduceComparison(logic)
      case QueryRightOuterJoin(path, logic) => "RIGHT OUTER JOIN " + reducePath(path) + " ON " + reduceComparison(logic)
      case QueryCrossJoin(path, logic) => "CROSS JOIN " + reducePath(path) + " ON " + reduceComparison(logic)
      case QueryFullOuterJoin(path, logic) => "FULL OUTER JOIN " + reducePath(path) + " ON " + reduceComparison(logic)
      case QueryInnerJoin(path, logic) => "INNER JOIN " + reducePath(path) + " ON " + reduceComparison(logic)
    }

    def reduceSort(sort: QuerySort): String = sort match {
      case QuerySortAsc(path) => reducePath(path) + " ASC"
      case QuerySortDesc(path) => reducePath(path) + " DESC"
    }

    val projections = query.values.map(reduceProjection).mkString(", ")
    val filter = query.filters.map("WHERE " + reduceComparison(_)).getOrElse("")
    val unions = query.unions.map(reduceUnion).mkString(" ")
    val sorts =
      if(query.sorts.isEmpty) ""
      else "ORDER BY " + query.sorts.map(reduceSort).mkString(", ")

    val groups =
      if(query.groupings.isEmpty) ""
      else "GROUP BY" + query.groupings.map(reduceSort).mkString(", ")

    val table = reducePath(query.table)

    s"SELECT $projections FROM $table $unions $filter $sorts $groups"
  }
}
