package scoobie.doobie

import doobie.imports._
import _root_.shapeless._
import scoobie.ast.QuerySelect

/**
  * Created by jacob.barber on 5/25/16.
  */
class QuerySelectExtensions[A <: HList: Param](expr: QuerySelect[A])(implicit sqlInterpreter: SqlInterpreter) {
  def queryAndPrint[B: Composite](printer: String => Unit): Query0[B] = sqlInterpreter.query[A, B](expr, printer)
  def query[B: Composite]: Query0[B] = queryAndPrint[B](void)
}
