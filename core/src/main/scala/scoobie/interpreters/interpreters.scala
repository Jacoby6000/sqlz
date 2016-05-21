package scoobie

import _root_.shapeless._
import doobie.imports._
import doobie.syntax.string.{SqlInterpolator, Builder}
import scoobie.ast._

/**
 * Created by jacob.barber on 3/3/16.
 */
package object interpreters {

  def void[A](a: A): Unit = ()

  implicit class QueryExpressionExtensions[A <: HList: Param](expr: QueryExpression[A])(implicit sqlInterpreter: SqlInterpreter) {
    def builderAndPrint(printer: String => Unit): Builder[A] = sqlInterpreter.builder(expr, printer)
    def builder: Builder[A] = sqlInterpreter.builder(expr, void)

    def genAndPrintSql(printer: String => Unit): String = sqlInterpreter.genAndPrintSql(expr, printer)
    def genSql: String = sqlInterpreter.genAndPrintSql(expr, void)
  }

  implicit class QuerySelectExtensions[A <: HList: Param](expr: QuerySelect[A])(implicit sqlInterpreter: SqlInterpreter) {
    def queryAndPrint[B: Composite](printer: String => Unit = void): Query0[B] = sqlInterpreter.query[A, B](expr, printer)
    def query[B: Composite]: Query0[B] = sqlInterpreter.query[A, B](expr, void)
  }

  implicit class QueryModifyExtensions[A <: HList: Param](expr: QueryModify[A])(implicit sqlInterpreter: SqlInterpreter) {
    def updateAndPrint(printer: String => Unit = void): Update0 = sqlInterpreter.update[A](expr, printer)
    def update: Update0 = sqlInterpreter.update[A](expr, void)
  }

}
