package scoobie.doobie

import doobie.syntax.string.SqlInterpolator
import doobie.util.composite.Composite
import doobie.util.query.Query0
import doobie.util.update.Update0
import doobie.util.param.Param
import doobie.util.fragment.Fragment
import scoobie.ast.{QueryExpression, QueryModify, QuerySelect}
import shapeless.HList

/**
  * Created by jacob.barber on 5/25/16.
  */
case class SqlInterpreter(genSql: QueryExpression[_] => String) {
  def query[A <: HList: Param, B: Composite](ast: QuerySelect[A], printer: String => Unit): Query0[B] =
    builderFromSql(genAndPrintSql(ast, printer), ast.params).query[B]

  def update[A <: HList: Param](ast: QueryModify[A], printer: String => Unit): Update0 =
    builderFromSql(genAndPrintSql(ast, printer), ast.params).update

  def builder[A <: HList: Param](ast: QueryExpression[A], printer: String => Unit): Fragment =
    builderFromSql(genAndPrintSql(ast, printer), ast.params)

  def builderFromSql[A <: HList : Param](sqlString: String, params: A): Fragment =
    new SqlInterpolator(new StringContext(sqlString.split('?'): _*)).sql.applyProduct[A](params)

  def genAndPrintSql[A <: HList](ast: QueryExpression[A], printer: String => Unit): String = {
    val sqlString = genSql(ast)
    printer(sqlString)
    sqlString
  }
}
