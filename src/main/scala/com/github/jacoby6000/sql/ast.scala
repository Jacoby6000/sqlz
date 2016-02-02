package com.github.jacoby6000.sql

import com.github.jacoby6000.shapeless.proofs._
import shapeless.HList

/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  sealed trait SqlValue

  case class SqlString(value: String) extends SqlValue
  case class SqlBoolean(value: Boolean) extends SqlValue

  sealed trait SqlNumber extends SqlValue

  case class SqlInt(value: Int) extends SqlNumber
  case class SqlDouble(value: Double) extends SqlNumber


  case class Using(table: String, run: SqlAction)

  sealed trait SqlAction
  case class BulkInsert[A <: HList, B <: HList](columns: A, values: List[B])(implicit columnsValuesSameLength: A SameLengthAs B,
                                                                             columnsOnlyStrings: A ContainsOnly String,
                                                                             valuesOnlySqlValues: B ContainsOnly SqlValue) extends SqlAction

  case class Insert[A <: HList, B <: HList](columns: A, values: B)(implicit columnsValuesSameLength: A SameLengthAs B) extends SqlAction


  case class Update[A <: HList, B <: HList](update: A, where: B)(implicit setsContainOnlyUpdateValues: A ContainsOnlyKind UpdateValue,
                                                                 whereClausesOnlyContainFilters: B ContainsOnlyKind SqlFilter) extends SqlAction

  case class Select[A <: HList](columns: List[String], where: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlAction


  sealed trait SqlFilter[A]
  case class In[A <: SqlValue](column: String, values: List[A]) extends SqlFilter[A]
  case class Equal[A <: SqlValue](column: String, value: A) extends SqlFilter[A]
  case class NotEqual[A <: SqlValue](column: String, value: A) extends SqlFilter[A]
  case class LessThan[A <: SqlNumber](column: String, value: A) extends SqlFilter[A]
  case class GreaterThan[A <: SqlNumber](column: String, value: A) extends SqlFilter[A]
  case class LessThanOrEqual[A <: SqlNumber](column: String, value: A) extends SqlFilter[A]
  case class GreaterThanOrEqual[A <: SqlNumber](column: String, value: A) extends SqlFilter[A]
  case class Like[A <: SqlValue](column: String, value: A) extends SqlFilter[A]
  case class Between[A <: SqlNumber](column: String, min: A, max: A) extends SqlFilter[A]

  case class UpdateValue[A <: SqlValue](column: String, value: A)


}
