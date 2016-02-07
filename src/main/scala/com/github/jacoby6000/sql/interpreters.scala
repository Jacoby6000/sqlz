package com.github.jacoby6000.sql

import cats.free.Free
import com.github.jacoby6000.sql.ast._

/**
  * Created by jacob.barber on 2/2/16.
  */
object interpreters {
  def interpretToString(action: DatabaseAction): String = {
    val table = action.table

    def selectToString(columns: List[String], joins: List[SqlJoin], where: List[SqlFilter], groupBy: List[String], offset: Option[Int], limit: Option[Int]): String = {
      val baseSelect = s"SELECT ${columns.mkString(", ")} FROM $table"
      val whereString = where.map(filterToSqlString).mkString("WHERE ", " AND\n", "")
      val joinString = joins.map {
        case FullOuterJoin(otherTable, joinFilters) => buildJoin("FULL OUTER JOIN", otherTable, joinFilters)
        case LeftOuterJoin(otherTable, joinFilters) => buildJoin("LEFT JOIN", otherTable, joinFilters)
        case RightOuterJoin(otherTable, joinFilters) => buildJoin("RIGHT JOIN", otherTable, joinFilters)
        case InnerJoin(otherTable, joinFilters) => buildJoin("INNER JOIN", otherTable, joinFilters)
        case CrossJoin(otherTable, joinFilters) => buildJoin("CROSS JOIN", otherTable, joinFilters)
      }.mkString("\n")

      val groupByString = groupBy.mkString(", ")
      val offsetString = offset.map("SKIP " + _) getOrElse ""
      val limitString = limit.map("LIMIT " + _) getOrElse ""

      s"$baseSelect\n$joinString\n$whereString\n$offsetString\n$limitString"
    }

    def buildStandardOperation(operator: String): (SqlValue, SqlValue) => String = (s, v) => s"${sqlValueToSqlString(s)} $operator ${sqlValueToSqlString(v)}"


    def updateWithNoFilter(list: List[UpdateValue]) = s"UPDATE $table SET ${list.map(v => v.column + "=" + sqlValueToSqlString(v.value))}"

    def sqlValueToSqlString(sqlValue: SqlValue): String =
      sqlValue.fold(
        add = (a,b) => sqlValueToSqlString(a) + " + " + sqlValueToSqlString(b),
        sub = (a,b) => sqlValueToSqlString(a) + " - " + sqlValueToSqlString(b),
        div = (a,b) => sqlValueToSqlString(a) + " / " + sqlValueToSqlString(b),
        mul = (a,b) => sqlValueToSqlString(a) + " * " + sqlValueToSqlString(b)
      )(identity, _.surround('"'), _.toString, _.toString, _.toString, selectToString, "?")

    def buildJoin(joinType: String, tbl: String, filters: List[SqlFilter]): String = s"$joinType $tbl ON " + filters.map(filterToSqlString).mkString(" AND\n")

    def filterToSqlString(filter: SqlFilter): String =
      filter.fold(like               = buildStandardOperation("LIKE"),
                  equal              = buildStandardOperation("="),
                  notEqual           = buildStandardOperation("<>"),
                  lessThan           = buildStandardOperation("<"),
                  greaterThan        = buildStandardOperation(">"),
                  lessThanOrEqual    = buildStandardOperation("<="),
                  greaterThanOrEqual = buildStandardOperation(">="),
                  in = (col, values) => s"${sqlValueToSqlString(col)} in (${values.map(sqlValueToSqlString).mkString(", ")})",
                  between =  (col, min, max) => s"${sqlValueToSqlString(col)} BETWEEN ${sqlValueToSqlString(min)} AND ${sqlValueToSqlString(max)}")

    action.run match {
      case Insert(columns, values) =>
        s"INSERT INTO $table (${columns.mkString(",\n")}) values (${values.map(sqlValueToSqlString).mkString(",\n")})"

      case Select(cols, joins, where, group, offset, limit) =>
        selectToString(cols, joins, where, group, offset, limit)

      case Update(updates, Nil) =>
        updateWithNoFilter(updates)

      case Update(updates, filters) =>
        updateWithNoFilter(updates) + "WHERE " + filters.map(filterToSqlString).mkString(" AND ")

      case Delete(where) => s"DELETE FROM $table WHERE ${where.map(filterToSqlString).mkString(" AND ")}"

      case BulkInsert(_, _) => "" // TODO: Implement me
    }
  }



  private implicit class StringExtensions(s: String) {
    def surround(char: Char): String = s"$char$s$char"
  }
}
