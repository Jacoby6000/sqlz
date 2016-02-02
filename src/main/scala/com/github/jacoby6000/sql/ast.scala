package com.github.jacoby6000.sql

import com.github.jacoby6000.shapeless.proofs._
import shapeless.HList
import cats._

/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  trait ToSqlValue[A, B <: SqlValue] {
    def sqlValueOf(a: A): B
  }

  implicit class ToSqlValueOps[A, B](a: A)(implicit toSqlValue: ToSqlValue[A, B]) {
    def asSql = toSqlValue.sqlValueOf(a)
  }

  object ToSqlValue {
    def apply[A, B <: SqlValue](f: A => B): ToSqlValue[A, B] = new ToSqlValue[A, B] {
      def sqlValueOf(a: A): B = f(a)
    }
  }

  type ToSqlNumber[A] = ToSqlValue[A, _ <: SqlNumber]

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
  case class In[A](column: String, values: List[A]) extends SqlFilter[A]
  case class Equal[A](column: String, value: A) extends SqlFilter[A]
  case class NotEqual[A](column: String, value: A) extends SqlFilter[A]
  case class LessThan[A: ToSqlNumber](column: String, value: A) extends SqlFilter[A]
  case class GreaterThan[A: ToSqlNumber](column: String, value: A) extends SqlFilter[A]
  case class LessThanOrEqual[A: ToSqlNumber](column: String, value: A) extends SqlFilter[A]
  case class GreaterThanOrEqual[A: ToSqlNumber](column: String, value: A) extends SqlFilter[A]
  case class Like[A](column: String, value: A) extends SqlFilter[A]
  case class Between[A](column: String, min: A, max: A) extends SqlFilter[A]

  case class UpdateValue[A](column: String, value: A)


  implicit val sqlStringToSqlValue = ToSqlValue { s: SqlString => s }
  implicit val sqlIntToSqlValue = ToSqlValue { s: SqlInt => s }
  implicit val sqlNumberToSqlValue = ToSqlValue { s: SqlNumber => s }
  implicit val sqlDoubleToSqlValue = ToSqlValue { s: SqlDouble => s }
  implicit val sqlBoolToSqlValue = ToSqlValue { s: SqlBoolean => s }
}
