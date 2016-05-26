package scoobie.doobie

import doobie.imports._
import doobie.syntax.string.Builder
import _root_.shapeless._
import scoobie.ast.QueryExpression


/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryExpressionExtensions[A <: HList: Param](expr: QueryExpression[A])(implicit sqlInterpreter: SqlInterpreter) {
  def builderAndPrint(printer: String => Unit): Builder[A] = sqlInterpreter.builder(expr, printer)
  def builder: Builder[A] = builderAndPrint(void)

  def genAndPrintSql(printer: String => Unit): String = sqlInterpreter.genAndPrintSql(expr, printer)
  def genSql: String = genAndPrintSql(void)
}
