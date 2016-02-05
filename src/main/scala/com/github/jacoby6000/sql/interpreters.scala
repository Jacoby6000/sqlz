package com.github.jacoby6000.sql

import cats.free.Free
import com.github.jacoby6000.sql.ast._

/**
  * Created by jacob.barber on 2/2/16.
  */
object interpreters {
  def interpretToString(action: DatabaseAction): String = {
    def sqlValueToSqlString(sqlValue: SqlValue): String =
      sqlValue.fold(_.surround('"'), _.toString, _.toString, _.toString, "?")

    action.run match {
      case Insert(columns, values) =>
        s"insert into ${action.table} (${columns.mkString(",\n")}) values (${values.map(sqlValueToSqlString).mkString(",\n")})"
    }
  }



  private implicit class StringExtensions(s: String) {
    def surround(char: Char): String = s"$char$s$char"
  }
}
