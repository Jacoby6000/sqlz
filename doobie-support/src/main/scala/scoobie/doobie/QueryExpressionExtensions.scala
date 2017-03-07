package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QueryExpression


/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryExpressionExtensions[A <: HList: Param](expr: QueryExpression[A])(implicit sqlInterpreter: SqlInterpreter) {
  def builderAndPrint(printer: Fragment => Unit): Fragment = sqlInterpreter.builder(expr, printer)
  def builder: Fragment = builderAndPrint(void)

  def genAndPrintSql(printer: Fragment => Unit): Fragment = sqlInterpreter.genAndPrintSql(expr, printer)
  def genSql: Fragment = genAndPrintSql(void)
}
