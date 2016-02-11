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

  sealed trait SqlProjection
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
  case class SqlFunction(name: String, params: List[SqlValue]) extends SqlValue with SqlProjection

  case class SqlColumn(table: Option[String], columnName: String, alias: Option[String]) extends SqlProjection with SqlValue
  case object SqlAllColumns extends SqlProjection

  sealed trait SqlJoin
  case class SqlInnerJoin(table: String, on: SqlPredicate) extends SqlJoin
  case class SqlFullOuterJoin(table: String, on: SqlPredicate) extends SqlJoin
  case class SqlLeftOuterJoin(table: String, on: SqlPredicate) extends SqlJoin
  case class SqlRightOuterJoin(table: String, on: SqlPredicate) extends SqlJoin
  case class SqlCrossJoin(table: String, on: SqlPredicate) extends SqlJoin


  sealed trait SqlPredicate
  case class SqlAnd(left: SqlPredicate, right: SqlPredicate) extends SqlPredicate
  case class SqlOr(left: SqlPredicate, right: SqlPredicate) extends SqlPredicate
  case class SqlNot(filter: SqlPredicate) extends SqlPredicate
  case class SqlIn(column: SqlValue, values: List[SqlValue]) extends SqlPredicate
  case class SqlLike(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlEqual(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlNotEqual(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlLessThan(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlGreaterThan(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlLessThanOrEqual(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlGreaterThanOrEqual(column: SqlValue, value: SqlValue) extends SqlPredicate
  case class SqlBetween(column: SqlValue, min: SqlValue, max: SqlValue) extends SqlPredicate

  case class UpdateValue(column: String, value: SqlValue)

  sealed trait SqlAction
  case class BulkInsert private (table: String, columns: List[SqlColumn], values: List[List[SqlValue]]) extends SqlAction
  case class Insert private (table: String, columns: List[SqlColumn], values: List[SqlValue]) extends SqlAction
  case class Update(table: String, update: List[UpdateValue], where: SqlPredicate) extends SqlAction
  case class Select(table: String, columns: List[SqlProjection], joins: List[SqlJoin], where: Option[SqlPredicate], groupBy: List[SqlColumn], offset: Option[Int], limit: Option[Int]) extends SqlAction
  case class Delete(table: String, where: SqlPredicate) extends SqlAction

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
    def apply[A <: HList](table: String, hUpdate: A, where: SqlPredicate)
                                                 (implicit hUpdateContainOnlyUpdateValues: A ContainsOnly UpdateValue,
                                                           hUpdateToList: ToTraversable.Aux[A, List, UpdateValue]): Update = Update(table, hUpdate.toList, where)
  }

  object Select {
    def apply[A <: HList, B <: HList, C <: HList](table: String, hColumns: A, hJoins: B, where: Option[SqlPredicate], hGroupBy: C, offset: Option[Int], limit: Option[Int])
                                                             (implicit hColumnsContainsOnlyStrings: A ContainsOnly SqlProjection,
                                                                       hJoinsContainsOnlySqlJoin: B ContainsOnly SqlJoin,
                                                                       hGroupByContainsOnlyString: C ContainsOnly SqlColumn,
                                                                       hColumnsToLost: ToTraversable.Aux[A, List, SqlProjection],
                                                                       hJoinToList: ToTraversable.Aux[B, List, SqlJoin],
                                                                       hGroupByToList: ToTraversable.Aux[C, List, SqlColumn]): Select = Select(table, hColumns.toList, hJoins.toList, where, hGroupBy.toList, offset, limit)
  }

}
