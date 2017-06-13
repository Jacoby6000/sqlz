package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QuerySelect

/**
  * Created by jacob.barber on 5/25/16.
  */
class QuerySelectExtensions(expr: QuerySelect[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter) {
  def queryWithLogHandler[B: Composite](logHandler: LogHandler): Query0[B] = sqlInterpreter.query[B](expr, logHandler)
  def query[B: Composite](implicit logHandler: LogHandler = LogHandler.nop): Query0[B] = sqlInterpreter.query[B](expr, logHandler)
}
