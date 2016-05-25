package scoobie.dsl.weak

import _root_.shapeless._
import scoobie.ast._
import modify._
import scoobie.dsl.weak.sql.primitives.SqlDslStringInterpolators
import scoobie.dsl.weak.sql.query.modify

/**
 * Created by jacob.barber on 3/4/16.
 */
package object sql {

  implicit val stringExpr = RawExpressionHandler[String](identity)

  implicit def sqlDslStringInterpolatorConverter(ctx: StringContext): SqlDslStringInterpolators = new SqlDslStringInterpolators(ctx)

  def deleteFrom(table: QueryPath): DeleteBuilder = new DeleteBuilder(table)

  val select = selectPackage.SelectBuilderBuilder

  // Update DSL helpers
  def update(table: QueryPath) = new UpdateBuilder(table, HNil: HNil, QueryComparisonNop)

  // Insert DSL helpers
  def insertInto(table: QueryPath): InsertBuilder = new InsertBuilder(table)

  // Select/Query DSL helpers

  val `null`: QueryValue[HNil] = QueryNull
  val `*`: QueryProjection[HNil] = QueryProjectAll




  implicit def toQueryValue[A](a: A)(implicit ev: A =:!= QueryParameter[_], ev2: A =:!= QueryComparison[_]): QueryValue[A :: HNil] = QueryParameter(a)
  implicit def toQueryProjection(queryPath: QueryPath): QueryProjection[HNil] = QueryProjectOne(queryPath, None)

  def not[A <: HList](queryComparison: QueryComparison[A]): QueryNot[A] = QueryNot(queryComparison)


}
