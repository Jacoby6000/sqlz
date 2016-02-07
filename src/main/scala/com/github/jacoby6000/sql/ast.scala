package com.github.jacoby6000.sql

import com.github.jacoby6000.shapeless.proofs._
import shapeless.HList
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.tuple.ToList

import scala.annotation.tailrec

/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  case class DatabaseAction(table: String, run: SqlAction)
  object Using {
    def apply(table: String): SqlAction => DatabaseAction = q => DatabaseAction(table, q)
  }

  sealed trait SqlValue {
    def fold[B](literal: String => B,
                string: String => B,
                bool: Boolean => B,
                int: Int => B,
                double: Double => B,
                select: (List[String], List[SqlJoin], List[SqlFilter], List[String], Option[Int], Option[Int]) => B,
                prepared: => B)
               (add: (SqlValue, SqlValue) => B,
                sub: (SqlValue, SqlValue) => B,
                div: (SqlValue, SqlValue) => B,
                mul: (SqlValue, SqlValue) => B): B = SqlValue.fold(this)(literal, string, bool, int, double, select, prepared)(add, sub, div, mul)
  }
  case class SqlLiteral(value: String) extends SqlValue

  sealed trait SqlAlgebra extends SqlValue
  case class SqlAdd(a: SqlValue, b: SqlValue) extends SqlAlgebra
  case class SqlSub(a: SqlValue, b: SqlValue) extends SqlAlgebra
  case class SqlDiv(a: SqlValue, b: SqlValue) extends SqlAlgebra
  case class SqlMul(a: SqlValue, b: SqlValue) extends SqlAlgebra



  sealed trait SqlNumber extends SqlValue
  case object SqlPrepared extends SqlValue with SqlNumber

  case class SqlString(value: String) extends SqlValue
  case class SqlBoolean(value: Boolean) extends SqlValue
  case class SqlSubQuery(query: Select) extends SqlValue

  case class SqlInt(value: Int) extends SqlNumber
  case class SqlDouble(value: Double) extends SqlNumber

  object SqlValue {

    def fold[B](sqlValue: SqlValue)
               (literal: String => B,
                string: String => B,
                bool: Boolean => B,
                int: Int => B,
                double: Double => B,
                select: (List[String], List[SqlJoin], List[SqlFilter], List[String], Option[Int], Option[Int]) => B,
                prepared: => B)
               (add: (SqlValue, SqlValue) => B,
                sub: (SqlValue, SqlValue) => B,
                div: (SqlValue, SqlValue) => B,
                mul: (SqlValue, SqlValue) => B): B =
      sqlValue match {
        case SqlAdd(a, b) => add(a,b)
        case SqlMul(a, b) => mul(a,b)
        case SqlDiv(a, b) => div(a,b)
        case SqlSub(a, b) => sub(a,b)
        case SqlString(s) => string(s)
        case SqlBoolean(b) => bool(b)
        case SqlInt(i) => int(i)
        case SqlDouble(d) => double(d)
        case SqlLiteral(l) => literal(l)
        case SqlSubQuery(Select(columns, joins, where, groupBy, offset, limit)) => select(columns, joins, where, groupBy, offset, limit)
        case SqlPrepared => prepared
      }
  }

  sealed trait SqlJoin
  case class InnerJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class FullOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class LeftOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class RightOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class CrossJoin(table: String, on: List[SqlFilter]) extends SqlJoin


  sealed trait SqlFilter {
    def fold[B](in: (SqlValue, List[SqlValue]) => B,
                like: (SqlValue, SqlValue) => B,
                equal: (SqlValue, SqlValue) => B,
                notEqual: (SqlValue, SqlValue) => B,
                lessThan: (SqlValue, SqlValue) => B,
                greaterThan: (SqlValue, SqlValue) => B,
                lessThanOrEqual: (SqlValue, SqlValue) => B,
                greaterThanOrEqual: (SqlValue, SqlValue) => B,
                between: (SqlValue, SqlValue, SqlValue) => B): B = SqlFilter.fold(this)(in, like, equal, notEqual, lessThan, greaterThan, lessThanOrEqual, greaterThanOrEqual, between)
  }
  case class In(column: SqlValue, values: List[SqlValue]) extends SqlFilter
  case class Like(column: SqlValue, value: SqlValue) extends SqlFilter
  case class Equal(column: SqlValue, value: SqlValue) extends SqlFilter
  case class NotEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class LessThan(column: SqlValue, value: SqlValue) extends SqlFilter
  case class GreaterThan(column: SqlValue, value: SqlValue) extends SqlFilter
  case class LessThanOrEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class GreaterThanOrEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class Between(column: SqlValue, min: SqlValue, max: SqlValue) extends SqlFilter

  object SqlFilter {
    def fold[B](filter: SqlFilter)
               (in: (SqlValue, List[SqlValue]) => B,
                like: (SqlValue, SqlValue) => B,
                equal: (SqlValue, SqlValue) => B,
                notEqual: (SqlValue, SqlValue) => B,
                lessThan: (SqlValue, SqlValue) => B,
                greaterThan: (SqlValue, SqlValue) => B,
                lessThanOrEqual: (SqlValue, SqlValue) => B,
                greaterThanOrEqual: (SqlValue, SqlValue) => B,
                between: (SqlValue, SqlValue, SqlValue) => B): B =
      filter match {
        case In(column, values) => in(column, values)
        case Like(column, value) => like(column, value)
        case Equal(column, value) => equal(column, value)
        case NotEqual(column, value) => notEqual(column, value)
        case LessThan(column, value) => lessThan(column, value)
        case GreaterThan(column, value) => greaterThan(column, value)
        case LessThanOrEqual(column, value) => lessThanOrEqual(column, value)
        case GreaterThanOrEqual(column, value) => greaterThanOrEqual(column, value)
        case Between(column, min, max) => between(column, min, max)
      }
  }

  case class UpdateValue(column: String, value: SqlValue)

  sealed trait SqlAction {
    def fold[B](select: (List[String], List[SqlJoin], List[SqlFilter], List[String], Option[Int], Option[Int]) => B,
                update: (List[UpdateValue], List[SqlFilter]) => B,
                insert: (List[String], List[SqlValue]) => B,
                bulkInsert: (List[String], List[List[SqlValue]]) => B,
                delete: List[SqlFilter] => B): B = SqlAction.fold(this)(select, update, insert, bulkInsert, delete)
  }

  case class BulkInsert private (columns: List[String], values: List[List[SqlValue]]) extends SqlAction

  object BulkInsert {
    def apply[A <: HList, B <: HList](hColumns: A, hValues: List[B])
                                     (implicit columnsValuesSameLength: A SameLengthAs B,
                                               columnsOnlyStrings: A ContainsOnly String,
                                               hValuesOnlySqlValue: B ContainsOnly SqlValue,
                                               hValuesContainsSqlValues: A,
                                               hValuesToList: ToTraversable.Aux[B, List, SqlValue],
                                               hColumnsToList: ToTraversable.Aux[A, List, String]): BulkInsert = BulkInsert(hColumns.toList, hValues.map(_.toList))
  }


  case class Insert private (columns: List[String], values: List[SqlValue]) extends SqlAction

  object Insert {
    def apply[A <: HList, B <: HList](columns: A, values: B)
                                     (implicit columnsValuesSameLength: A SameLengthAs B,
                                               columnsOnlyStrings: A ContainsOnly String,
                                               hValuesOnlySqlValue: B ContainsOnly SqlValue,
                                               hValuesContainsSqlValues: A,
                                               hValuesToList: ToTraversable.Aux[B, List, SqlValue],
                                               hColumnsToList: ToTraversable.Aux[A, List, String]): Insert = Insert(columns.toList, values.toList)
  }

  case class Update private (update: List[UpdateValue], where: List[SqlFilter]) extends SqlAction

  object Update {
    def apply[A <: HList, B <: HList, C <: HList](hUpdate: A, hWhere: B)
                                                 (implicit hUpdateContainOnlyUpdateValues: A ContainsOnly UpdateValue,
                                                           hWhereOnlyContainFilters: B ContainsOnly SqlFilter,
                                                           hUpdateToList: ToTraversable.Aux[A, List, UpdateValue],
                                                           hWhereToList: ToTraversable.Aux[B, List, SqlFilter]): Update = Update(hUpdate.toList, hWhere.toList)
  }

  case class Select(columns: List[String], joins: List[SqlJoin], where: List[SqlFilter], groupBy: List[String], offset: Option[Int], limit: Option[Int]) extends SqlAction
  object Select {
    def apply[A <: HList, B <: HList, C <: HList, D <: HList](hColumns: A, hJoins: B, hWhere: C, hGroupBy: D, offset: Option[Int], limit: Option[Int])
                                                             (implicit hColumnsContainsOnlyStrings: A ContainsOnly String,
                                                                       hJoinsContainsOnlySqlJoin: B ContainsOnly SqlJoin,
                                                                       hWhereContainsOnlySqlFilter: C ContainsOnly SqlFilter,
                                                                       hGroupByContainsOnlyString: D ContainsOnly String,
                                                                       hColumnsToLost: ToTraversable.Aux[A, List, String],
                                                                       hJoinToList: ToTraversable.Aux[B, List, SqlJoin],
                                                                       hWheretoList: ToTraversable.Aux[C, List, SqlFilter],
                                                                       hGroupByToList: ToTraversable.Aux[D, List, String]): Select = Select(hColumns.toList, hJoins.toList, hWhere.toList, hGroupBy.toList, offset, limit)
  }

  case class Delete(where: List[SqlFilter]) extends SqlAction

  object Delete {
    def apply[A <: HList](hWhere: A)(implicit hWhereContainsOnlySqlFilter: A ContainsOnly SqlFilter,
                                              hWhereToList: ToTraversable.Aux[A, List, SqlFilter]): Delete = Delete(hWhere.toList)
  }

  object SqlAction {
    def fold[B](action: SqlAction)
               (select: (List[String], List[SqlJoin], List[SqlFilter], List[String], Option[Int], Option[Int]) => B,
                update: (List[UpdateValue], List[SqlFilter]) => B,
                insert: (List[String], List[SqlValue]) => B,
                bulkInsert: (List[String], List[List[SqlValue]]) => B,
                delete: List[SqlFilter] => B): B =
      action match {
        case Select(columns, joins, where, groupBy, offset, limit) => select(columns, joins, where, groupBy, offset, limit)
        case Update(values, where) => update(values, where)
        case Insert(columns, values) => insert(columns, values)
        case BulkInsert(columns, values) => bulkInsert(columns, values)
        case Delete(where) => delete(where)
      }
  }

}
