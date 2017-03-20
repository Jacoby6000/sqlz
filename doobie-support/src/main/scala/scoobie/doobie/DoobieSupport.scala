package scoobie.doobie

import doobie.imports._
import scoobie.ast._
import shapeless._

/**
  * Created by jacob.barber on 5/25/16.
  */
trait DoobieSupport {
  implicit def toQuerySelectExtensions(expr: QuerySelect[ScoobieFragmentProducer])(implicit sqlInterpreter: SqlInterpreter): QuerySelectExtensions = new QuerySelectExtensions(expr)
  implicit def toQueryExpressionExtensions(expr: QueryExpression[ScoobieFragmentProducer])(implicit sqlInterpreter: SqlInterpreter): QueryExpressionExtensions = new QueryExpressionExtensions(expr)
  implicit def toQueryModifyExtensions(expr: QueryModify[ScoobieFragmentProducer])(implicit sqlInterpreter: SqlInterpreter): QueryModifyExtensions = new QueryModifyExtensions(expr)
}


