package com.github.jacoby6000.query

import com.github.jacoby6000.query.ast._

/**
  * Created by jacob.barber on 3/3/16.
  */
object interpreter {
  def interpretSql(query: Query): String = {
    def binOpReduction[A](op: String, left: A, right: A)(f: A => String) = f(left) + " " + op + " " + f(right)

    def reducePath(queryPath: QueryPath): String = queryPath match {
      case QueryPathEnd(str) => str
      case QueryPathCons(head, tail) => head + "." + reducePath(tail)
    }

    def reduceProjection(projection: QueryProjection): String = projection match {
      case QueryProjectAll => "*"
      case QueryProjectOne(path, alias) => reduceValue(path) + alias.map(" AS " + _).getOrElse("")
    }

    def reduceValue(value: QueryValue): String = value match {
      case QueryString(s) => "\"" + s + "\""
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
      else "GROUP BY " + query.groupings.map(reduceSort).mkString(", ")

    val table = reducePath(query.table)

    s"SELECT $projections FROM $table $unions $filter $sorts $groups"
  }
}
