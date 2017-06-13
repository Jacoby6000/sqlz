package scoobie.doobie

import doobie.imports._
import scoobie.ast._
import shapeless._

/**
  * Created by jacob.barber on 5/25/16.
  */
trait DoobieSupport {
  implicit def toQuerySelectExtensions(expr: QuerySelect[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter): QuerySelectExtensions = new QuerySelectExtensions(expr)
  implicit def toQueryExpressionExtensions(expr: QueryExpression[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter): QueryExpressionExtensions = new QueryExpressionExtensions(expr)
  implicit def toQueryModifyExtensions(expr: QueryModify[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter): QueryModifyExtensions = new QueryModifyExtensions(expr)
}


