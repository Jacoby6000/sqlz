package scoobie.doobie

import doobie.syntax.string.SqlInterpolator
import doobie.util.composite.Composite
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.util.fragment.Fragment
import doobie.util.log.LogHandler
import scoobie.ast.{QueryExpression, QueryModify, QuerySelect}
import shapeless.HList

/**
  * Created by jacob.barber on 5/25/16.
  */
case class SqlInterpreter(genSql: QueryExpression[ScoobieFragmentProducer] => Fragment) {
  def query[B: Composite](ast: QuerySelect[ScoobieFragmentProducer], printer: Fragment => Unit): Query0[B] =
    genAndPrintSql(ast, printer).queryWithLogHandler[B](LogHandler.jdkLogHandler)

  def update(ast: QueryModify[ScoobieFragmentProducer], printer: Fragment => Unit): Update0 =
    genAndPrintSql(ast, printer).updateWithLogHandler(LogHandler.jdkLogHandler)

  def genAndPrintSql(ast: QueryExpression[ScoobieFragmentProducer], printer: Fragment => Unit): Fragment = {
    val sqlString = genSql(ast)
    printer(sqlString)
    sqlString
  }
}
