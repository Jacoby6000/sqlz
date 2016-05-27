package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QueryModify

/**
  * Created by jacob.barber on 5/25/16.
  */
class QueryModifyExtensions[A <: HList: Param](expr: QueryModify[A])(implicit sqlInterpreter: SqlInterpreter) {
  def updateAndPrint(printer: String => Unit): Update0 = sqlInterpreter.update[A](expr, printer)
  def update: Update0 = updateAndPrint(void)
}
