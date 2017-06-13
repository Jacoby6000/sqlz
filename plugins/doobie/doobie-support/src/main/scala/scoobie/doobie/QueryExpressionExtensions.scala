package scoobie.doobie

import doobie.imports._
import scoobie.ast.QueryExpression


/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryExpressionExtensions(expr: QueryExpression[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter) {
  def build: Fragment = sqlInterpreter.genSql(expr)
  def genFragment: Fragment = build
}
