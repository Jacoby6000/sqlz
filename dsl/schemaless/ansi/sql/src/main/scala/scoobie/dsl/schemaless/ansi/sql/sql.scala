package scoobie.dsl.schemaless.ansi

import scoobie.ast.ansi._
import scoobie.ast.coercion.Coerce

package object sql extends query.modify with query.select with primitives {

  implicit val stringExpr = RawExpressionHandler[String](identity)

  implicit def sqlDslStringInterpolatorConverter[F[_]](ctx: StringContext)(implicit coerce: Coerce[F]): SqlDslStringInterpolators[F] = new SqlDslStringInterpolators(ctx)
  implicit def sqlValueExtensions[F[_]](a: QueryValue[F]): QueryValueExtensions[F] = new QueryValueExtensions(a)
  implicit def sqlComparisonExtensions[F[_]](a: QueryComparison[F]): QueryComparisonExtensions[F] = new QueryComparisonExtensions(a)
  implicit def sqlProjectionExtensions[F[_]](a: QueryProjection[F])(implicit coerce: Coerce[F]): QueryProjectionExtensions[F] = new QueryProjectionExtensions(a)
  implicit def sqlModifyFieldBuilder[F[_]](a: QueryPath[F]): ModifyFieldBuilder[F] = ModifyFieldBuilder(a)
  implicit def sqlSortBuilder[F[_]](a: QueryPath[F]): QuerySortBuilder[F] = new QuerySortBuilder(a)

  def deleteFrom[F[_]](table: QueryPath[F]): DeleteBuilder[F] = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  def update[F[_]](table: QueryPath[F]): UpdateBuilder[F] = new UpdateBuilder(table, List.empty, QueryComparisonNop[F])
  def insertInto[F[_]](table: QueryPath[F]): InsertBuilder[F] = new InsertBuilder(table)

  def `null`[F[_]]: QueryValue[F] = QueryNull[F]
  def  `*`[F[_]]: QueryProjection[F] = QueryProjectAll[F]

  def not[F[_]](queryComparison: QueryComparison[F]): QueryNot[F] = QueryNot(queryComparison)

  implicit def toQueryValue[F[_], A](a: A)(
    implicit
    $e: Coerce[F],
    ev3: F[A]
  ): QueryValue[F] =
    QueryParameter(a)

  implicit def pathToQueryProjection[F[_]](queryPath: QueryPath[F]): QueryProjection[F] =
    QueryProjectOne(queryPath, None)

  implicit def valueToQueryProjection[F[_]](value: QueryValue[F]): QueryProjection[F] =
    QueryProjectOne(value, None)

  implicit def queryBuilderToProjection[F[_]](builder: QueryBuilder[F]): QueryProjection[F] =
    QueryProjectOne(builder.build, None)
}
