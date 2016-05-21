package scoobie.doobie

import doobie.imports._
import doobie.syntax.string.{SqlInterpolator, Builder}
import scoobie.ast.{QueryModify, QuerySelect, QueryExpression}
import shapeless.HList

/**
  * Created by jbarber on 5/21/16.
  */
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
