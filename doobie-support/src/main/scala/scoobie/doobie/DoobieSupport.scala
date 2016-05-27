package scoobie.doobie

import doobie.imports._
import doobie.syntax.string.{Builder, SqlInterpolator}
import scoobie.ast._
import shapeless._

/**
  * Created by jacob.barber on 5/25/16.
  */
trait DoobieSupport {
  implicit def toQuerySelectExtensions[A <: HList: Param](expr: QuerySelect[A])(implicit sqlInterpreter: SqlInterpreter): QuerySelectExtensions[A] = new QuerySelectExtensions[A](expr)
  implicit def toQueryExpressionExtensions[A <: HList: Param](expr: QueryExpression[A])(implicit sqlInterpreter: SqlInterpreter): QueryExpressionExtensions[A] = new QueryExpressionExtensions[A](expr)
  implicit def toQueryModifyExtensions[A <: HList: Param](expr: QueryModify[A])(implicit sqlInterpreter: SqlInterpreter): QueryModifyExtensions[A] = new QueryModifyExtensions[A](expr)
}


