package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QueryModify

/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryModifyExtensions(expr: QueryModify[ScoobieFragmentProducer])(implicit sqlInterpreter: SqlInterpreter) {
  def updateAndPrint(printer: Fragment => Unit): Update0 = sqlInterpreter.update(expr, printer)
  def update: Update0 = updateAndPrint(void)
}
