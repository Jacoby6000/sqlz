package scoobie

import _root_.doobie.imports._
import _root_.doobie.syntax.string.{Builder, SqlInterpolator}
import _root_.shapeless.HList
import scoobie.ast.{QueryExpression, QueryModify, QuerySelect}

/**
  * Created by jbarber on 5/21/16.
  */
package object doobie {

  def void[A](a: A): Unit = ()

  implicit class QueryExpressionExtensions[A <: HList: Param](expr: QueryExpression[A])(implicit sqlInterpreter: SqlInterpreter) {
    def builderAndPrint(printer: String => Unit): Builder[A] = sqlInterpreter.builder(expr, printer)
    def builder: Builder[A] = builderAndPrint(void)

    def genAndPrintSql(printer: String => Unit): String = sqlInterpreter.genAndPrintSql(expr, printer)
    def genSql: String = genAndPrintSql(void)
  }

  implicit class QuerySelectExtensions[A <: HList: Param](expr: QuerySelect[A])(implicit sqlInterpreter: SqlInterpreter) {
    def queryAndPrint[B: Composite](printer: String => Unit): Query0[B] = sqlInterpreter.query[A, B](expr, printer)
    def query[B: Composite]: Query0[B] = queryAndPrint[B](void)
  }

  implicit class QueryModifyExtensions[A <: HList: Param](expr: QueryModify[A])(implicit sqlInterpreter: SqlInterpreter) {
    def updateAndPrint(printer: String => Unit): Update0 = sqlInterpreter.update[A](expr, printer)
    def update: Update0 = updateAndPrint(void)
  }


}
