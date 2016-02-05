package com.github.jacoby6000.sql

import com.github.jacoby6000.shapeless.proofs._
import shapeless.HList
import shapeless.ops.hlist.ToTraversable
import shapeless.ops.tuple.ToList

/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  case class DatabaseAction(table: String, run: SqlAction)
  object Using {
    def apply(table: String): SqlAction => DatabaseAction = q => DatabaseAction(table, q)
  }

  sealed trait SqlValue {
    def fold[B](string: String => B,
                bool: Boolean => B,
                int: Int => B,
                double: Double => B,
                prepared: => B): B = SqlValue.fold(this)(string, bool, int, double, prepared)
  }
  sealed trait SqlNumber extends SqlValue
  case object SqlPrepared extends SqlValue with SqlNumber

  case class SqlString(value: String) extends SqlValue
  case class SqlBoolean(value: Boolean) extends SqlValue

  case class SqlInt(value: Int) extends SqlNumber
  case class SqlDouble(value: Double) extends SqlNumber

  object SqlValue {
    def fold[B](sqlValue: SqlValue)
               (string: String => B,
                bool: Boolean => B,
                int: Int => B,
                double: Double => B,
                prepared: => B): B = sqlValue match {
      case SqlString(s) => string(s)
      case SqlBoolean(b) => bool(b)
      case SqlInt(i) => int(i)
      case SqlDouble(d) => double(d)
      case SqlPrepared => prepared
    }
  }

  sealed trait SqlJoin
  case class InnerJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class FullOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class LeftOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class RightOuterJoin(table: String, on: List[SqlFilter]) extends SqlJoin
  case class CrossJoin(table: String, on: List[SqlFilter]) extends SqlJoin


  sealed trait SqlFilter
  case class In(column: String, values: List[SqlValue]) extends SqlFilter
  case class Like(column: String, value: SqlValue) extends SqlFilter
  case class Equal(column: String, value: SqlValue) extends SqlFilter
  case class NotEqual(column: String, value: SqlValue) extends SqlFilter
  case class LessThan(column: String, value: SqlValue) extends SqlFilter
  case class GreaterThan(column: String, value: SqlValue) extends SqlFilter
  case class LessThanOrEqual(column: String, value: SqlValue) extends SqlFilter
  case class GreaterThanOrEqual(column: String, value: SqlValue) extends SqlFilter
  case class Between(column: String, min: SqlValue, max: SqlValue) extends SqlFilter

  case class UpdateValue(column: String, value: SqlValue)

  sealed trait SqlAction

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
                                                           hWhereToList: ToTraversable.Aux[B, List, SqlFilter]) = new Update(hUpdate.toList, hWhere.toList)
  }

  case class Select(columns: List[String], joins: List[SqlJoin], where: List[SqlFilter], groupBy: List[String], offset: Int, limit: Int) extends SqlAction
  case class Delete[A <: HList](where: List[SqlFilter]) extends SqlAction
}
