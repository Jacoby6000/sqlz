package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QueryModify

/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryModifyExtensions(expr: QueryModify[ScoobieFragmentProducer])(implicit sqlInterpreter: DoobieSqlInterpreter) {
  def updateWithLogHandler(logHandler: LogHandler): Update0 = sqlInterpreter.update(expr, logHandler)
  def update(implicit logHandler: LogHandler = LogHandler.nop): Update0 = sqlInterpreter.update(expr, logHandler)
}
