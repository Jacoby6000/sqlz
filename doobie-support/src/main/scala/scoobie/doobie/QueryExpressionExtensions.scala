package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QueryExpression


/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryExpressionExtensions(expr: QueryExpression[ScoobieFragmentProducer])(implicit sqlInterpreter: SqlInterpreter) {
  def build: Fragment = sqlInterpreter.genSql(expr)
  def genFragment: Fragment = build
}
