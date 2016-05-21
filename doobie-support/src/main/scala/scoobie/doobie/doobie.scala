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

  case class SqlInterpreter(genSql: QueryExpression[_] => String) {
    def query[A <: HList: Param, B: Composite](ast: QuerySelect[A], printer: String => Unit): Query0[B] =
      builderFromSql(genAndPrintSql(ast, printer), ast.params).query[B]

    def update[A <: HList: Param](ast: QueryModify[A], printer: String => Unit): Update0 =
      builderFromSql(genAndPrintSql(ast, printer), ast.params).update

    def builder[A <: HList: Param](ast: QueryExpression[A], printer: String => Unit): Builder[A] =
      builderFromSql(genAndPrintSql(ast, printer), ast.params)

    def builderFromSql[A <: HList : Param](sqlString: String, params: A) =
      new SqlInterpolator(new StringContext(sqlString.split('?'): _*)).sql.applyProduct[A](params)

    def genAndPrintSql[A <: HList](ast: QueryExpression[A], printer: String => Unit): String = {
      val sqlString = genSql(ast)
      printer(sqlString)
      sqlString
    }
  }

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
