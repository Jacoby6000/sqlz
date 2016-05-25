package scoobie.dsl.weak

import _root_.shapeless._
import scoobie.ast._
import scoobie.dsl.weak.sql.primitives._

/**
 * Created by jacob.barber on 3/4/16.
 */
package object sql extends query.modify with query.select {

  implicit val stringExpr = RawExpressionHandler[String](identity)

  implicit def sqlDslStringInterpolatorConverter(ctx: StringContext): SqlDslStringInterpolators = new SqlDslStringInterpolators(ctx)
  implicit def sqlValueExtensions[A <: HList](a: QueryValue[A]): QueryValueExtensions[A] = new QueryValueExtensions(a)
  implicit def sqlComparisonExtensions[A <: HList](a: QueryComparison[A]): QueryComparisonExtensions[A] = new QueryComparisonExtensions(a)
  implicit def sqlProjectionExtensions[A <: HList](a: QueryProjection[A]): QueryProjectionExtensions[A] = new QueryProjectionExtensions(a)
  implicit def sqlModifyFieldBuilder(a: QueryPath): ModifyFieldBuilder = ModifyFieldBuilder(a)
  implicit def sqlSortBuilder(a: QueryPath): QuerySortBuilder = new QuerySortBuilder(a)

  def deleteFrom(table: QueryPath): DeleteBuilder = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  // Update DSL helpers
  def update(table: QueryPath) = new UpdateBuilder(table, hnil, QueryComparisonNop)

  // Insert DSL helpers
  def insertInto(table: QueryPath): InsertBuilder = new InsertBuilder(table)

  // Select/Query DSL helpers

  val `null`: QueryValue[HNil] = QueryNull
  val `*`: QueryProjection[HNil] = QueryProjectAll

  def not[A <: HList](queryComparison: QueryComparison[A]): QueryNot[A] = QueryNot(queryComparison)

  implicit def toQueryValue[A](a: A)(implicit ev: A =:!= QueryParameter[_], ev2: A =:!= QueryComparison[_]): QueryValue[A :: HNil] = QueryParameter(a)
  implicit def toQueryProjection(queryPath: QueryPath): QueryProjection[HNil] = QueryProjectOne(queryPath, None)

  def hnil: HNil = HNil
}
