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

  case class DatabaseAction(database: String, run: SqlAction)

  sealed trait SqlValue
  case object SqlPrepared extends SqlValue
  case class SqlAdd(a: SqlValue, b: SqlValue) extends SqlValue
  case class SqlSub(a: SqlValue, b: SqlValue) extends SqlValue
  case class SqlDiv(a: SqlValue, b: SqlValue) extends SqlValue
  case class SqlMul(a: SqlValue, b: SqlValue) extends SqlValue
  case class SqlString(value: String) extends SqlValue
  case class SqlBoolean(value: Boolean) extends SqlValue
  case class SqlSubQuery(query: Select) extends SqlValue
  case class SqlInt(value: Int) extends SqlValue
  case class SqlDouble(value: Double) extends SqlValue
  case class SqlColumn(table: Option[String], columnName: String) extends SqlValue
  case class SqlFunction(name: String, params: List[SqlValue]) extends SqlValue

  sealed trait SqlJoin
  case class InnerJoin(table: String, on: SqlFilter) extends SqlJoin
  case class FullOuterJoin(table: String, on: SqlFilter) extends SqlJoin
  case class LeftOuterJoin(table: String, on: SqlFilter) extends SqlJoin
  case class RightOuterJoin(table: String, on: SqlFilter) extends SqlJoin
  case class CrossJoin(table: String, on: SqlFilter) extends SqlJoin


  sealed trait SqlFilter
  case class And(left: SqlFilter, right: SqlFilter) extends SqlFilter
  case class Or(left: SqlFilter, right: SqlFilter) extends SqlFilter
  case class Not(filter: SqlFilter) extends SqlFilter
  case class In(column: SqlValue, values: List[SqlValue]) extends SqlFilter
  case class Like(column: SqlValue, value: SqlValue) extends SqlFilter
  case class Equal(column: SqlValue, value: SqlValue) extends SqlFilter
  case class NotEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class LessThan(column: SqlValue, value: SqlValue) extends SqlFilter
  case class GreaterThan(column: SqlValue, value: SqlValue) extends SqlFilter
  case class LessThanOrEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class GreaterThanOrEqual(column: SqlValue, value: SqlValue) extends SqlFilter
  case class Between(column: SqlValue, min: SqlValue, max: SqlValue) extends SqlFilter

  case class UpdateValue(column: String, value: SqlValue)

  sealed trait SqlAction
  case class BulkInsert private (table: String, columns: List[SqlColumn], values: List[List[SqlValue]]) extends SqlAction
  case class Insert private (table: String, columns: List[SqlColumn], values: List[SqlValue]) extends SqlAction
  case class Update(table: String, update: List[UpdateValue], where: SqlFilter) extends SqlAction
  case class Select(table: String, columns: List[SqlColumn], joins: List[SqlJoin], where: Option[SqlFilter], groupBy: List[SqlColumn], offset: Option[Int], limit: Option[Int]) extends SqlAction
  case class Delete(table: String, where: SqlFilter) extends SqlAction

  // Companion objects below provide helper constructors so that everything can be constructed safely, or in similar fashions to other objects.

  object BulkInsert {
    def apply[A <: HList, B <: HList](table: String, hColumns: A, hValues: List[B])
                                     (implicit columnsValuesSameLength: A SameLengthAs B,
                                               columnsOnlyStrings: A ContainsOnly SqlColumn,
                                               hValuesOnlySqlValue: B ContainsOnly SqlValue,
                                               hValuesContainsSqlValues: A,
                                               hValuesToList: ToTraversable.Aux[B, List, SqlValue],
                                               hColumnsToList: ToTraversable.Aux[A, List, SqlColumn]): BulkInsert = BulkInsert(table, hColumns.toList, hValues.map(_.toList))
  }



  object Insert {
    def apply[A <: HList, B <: HList](table: String, columns: A, values: B)
                                     (implicit columnsValuesSameLength: A SameLengthAs B,
                                               columnsOnlyStrings: A ContainsOnly SqlColumn,
                                               hValuesOnlySqlValue: B ContainsOnly SqlValue,
                                               hValuesContainsSqlValues: A,
                                               hValuesToList: ToTraversable.Aux[B, List, SqlValue],
                                               hColumnsToList: ToTraversable.Aux[A, List, SqlColumn]): Insert = Insert(table, columns.toList, values.toList)
  }


  object Update {
    def apply[A <: HList](table: String, hUpdate: A, where: SqlFilter)
                                                 (implicit hUpdateContainOnlyUpdateValues: A ContainsOnly UpdateValue,
                                                           hUpdateToList: ToTraversable.Aux[A, List, UpdateValue]): Update = Update(table, hUpdate.toList, where)
  }

  object Select {
    def apply[A <: HList, B <: HList, C <: HList](table: String, hColumns: A, hJoins: B, where: Option[SqlFilter], hGroupBy: C, offset: Option[Int], limit: Option[Int])
                                                             (implicit hColumnsContainsOnlyStrings: A ContainsOnly SqlColumn,
                                                                       hJoinsContainsOnlySqlJoin: B ContainsOnly SqlJoin,
                                                                       hGroupByContainsOnlyString: C ContainsOnly SqlColumn,
                                                                       hColumnsToLost: ToTraversable.Aux[A, List, SqlColumn],
                                                                       hJoinToList: ToTraversable.Aux[B, List, SqlJoin],
                                                                       hGroupByToList: ToTraversable.Aux[C, List, SqlColumn]): Select = Select(table, hColumns.toList, hJoins.toList, where, hGroupBy.toList, offset, limit)
  }

}
