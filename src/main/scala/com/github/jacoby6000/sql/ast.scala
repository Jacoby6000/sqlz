package com.github.jacoby6000.sql

import com.github.jacoby6000.shapeless.proofs._
import shapeless.HList

/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  case class Using(table: String, run: SqlAction)
  object Using {
    def apply(table: String): SqlAction => Using = q => Using(table, q)
  }

  sealed trait SqlJoin[A]
  case class InnerJoin[A <: HList](table: String, on: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlJoin[A]
  case class FullOuterJoin[A <: HList](table: String, on: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlJoin[A]
  case class LeftOuterJoin[A <: HList](table: String, on: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlJoin[A]
  case class RightOuterJoin[A <: HList](table: String, on: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlJoin[A]
  case class CrossJoin[A <: HList](table: String, on: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter) extends SqlJoin[A]

  sealed trait SqlFilter[A]
  case class In[A](column: String, values: List[A]) extends SqlFilter[A]
  case class Equal[A](column: String, value: A) extends SqlFilter[A]
  case class NotEqual[A](column: String, value: A) extends SqlFilter[A]
  case class LessThan[A](column: String, value: A) extends SqlFilter[A]
  case class GreaterThan[A](column: String, value: A) extends SqlFilter[A]
  case class LessThanOrEqual[A](column: String, value: A) extends SqlFilter[A]
  case class GreaterThanOrEqual[A](column: String, value: A) extends SqlFilter[A]
  case class Like[A](column: String, value: A) extends SqlFilter[A]
  case class Between[A](column: String, min: A, max: A) extends SqlFilter[A]

  case class UpdateValue[A](column: String, value: A)

  sealed trait SqlAction

  case class BulkInsert[A <: HList, B <: HList](columns: A, values: List[B])(implicit columnsValuesSameLength: A SameLengthAs B,
                                                                             columnsOnlyStrings: A ContainsOnly String) extends SqlAction

  case class Insert[A <: HList, B <: HList](columns: A, values: B)(implicit columnsValuesSameLength: A SameLengthAs B) extends SqlAction

  case class Update[A <: HList, B <: HList, C <: HList](update: A, where: B)(implicit setsContainOnlyUpdateValues: A ContainsOnlyKind UpdateValue,
                                                                             whereClausesOnlyContainFilters: B ContainsOnlyKind SqlFilter) extends SqlAction

  case class Select[A <: HList, B <: HList](columns: List[String], where: A, joins: B)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter,
                                                                                       joinsContainOnlyJoins: B ContainsOnlyKind SqlJoin) extends SqlAction

  case class Delete[A <: HList](where: A)(implicit whereClausesOnlyContainFilters: A ContainsOnlyKind SqlFilter)

}
