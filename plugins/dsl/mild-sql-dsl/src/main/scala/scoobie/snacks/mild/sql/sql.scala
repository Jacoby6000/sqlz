package scoobie.snacks.mild

import scoobie.ast._

package object sql extends query.modify with query.select with primitives {


  implicit def sqlDslStringInterpolatorConverter[A, B, C](ctx: StringContext)(implicit coerce: Coerce[A, B, C]): SqlDslStringInterpolators[A] = new SqlDslStringInterpolators(ctx)
  implicit def sqlValueExtensions[A, B](a: QueryValue[A, B]): QueryValueExtensions[A, B] = new QueryValueExtensions(a)
  implicit def sqlComparisonExtensions[A, B](a: QueryComparison[A, B]): QueryComparisonExtensions[A, B] = new QueryComparisonExtensions(a)
  implicit def sqlProjectionExtensions[A](a: QueryProjection[A]): QueryProjectionExtensions[A] = new QueryProjectionExtensions(a)
  implicit def sqlModifyFieldBuilder(a: QueryPath): ModifyFieldBuilder = ModifyFieldBuilder(a)
  implicit def sqlSortBuilder(a: QueryPath): QuerySortBuilder = new QuerySortBuilder(a)

  def deleteFrom(table: QueryPath): DeleteBuilder = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  def update[A, B](table: QueryPath): UpdateBuilder[G, A, B] = new UpdateBuilder(table, List.empty, QueryComparisonNop[A, B])
  def insertInto(table: QueryPath): InsertBuilder = new InsertBuilder(table)

  def `null`[A, B]: QueryValue[A, B] = QueryNull[A, B]
  def  `*`[A]: QueryProjection[A] = QueryProjectAll[A]


  implicit def toQueryValue[F[_], A, B, C, T](t: T)(
    implicit
    ev: Coerce[A, B, C],
    qt: QueryType[F, B],
    ft: F[T]
  ): QueryValue[A, B] =
    QueryParameter[A, B](qt.toQueryType(t, ft))

  implicit def pathToValue[A, B](queryPath: QueryPath): QueryValue[A, B] =
    QueryPathValue(queryPath)

  implicit def pathToQueryProjection[A, B](queryPath: QueryPath): QueryProjection[QueryValue[A, B]] =
    QueryProjectOne(pathToValue[A, B](queryPath), None)

  implicit def valueToQueryProjection[A, B](value: QueryValue[A, B]): QueryProjection[QueryValue[A, B]] =
    QueryProjectOne(value, None)

  implicit def queryBuilderToProjection[A, B](builder: QueryBuilder[A, B]): QueryProjection[A, B] =
    QueryProjectOne(builder.build, None)
}
