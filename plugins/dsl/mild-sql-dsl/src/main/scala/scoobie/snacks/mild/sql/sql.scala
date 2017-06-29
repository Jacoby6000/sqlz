package scoobie.snacks.mild

import scoobie.ast._
import scoobie.coercion.Coerce
import scoobie.snacks.TypeComparisons._
import scoobie.snacks.Pointed
import scoobie.snacks.Pointed.PointedOps

package object sql extends query.modify with query.select with primitives {


  implicit val stringExpr = RawExpressionHandler[String](identity)

  implicit def sqlDslStringInterpolatorConverter[F[_]](ctx: StringContext)(implicit coerce: Coerce[F]): SqlDslStringInterpolators[F] = new SqlDslStringInterpolators(ctx)
  implicit def sqlValueExtensions[F[_], A](a: QueryValue[F, A]): QueryValueExtensions[F, A] = new QueryValueExtensions(a)
  implicit def sqlComparisonExtensions[F[_], A](a: QueryComparison[F, A]): QueryComparisonExtensions[F, A] = new QueryComparisonExtensions(a)
  implicit def sqlProjectionExtensions[A](a: QueryProjection[A]): QueryProjectionExtensions[A] = new QueryProjectionExtensions(a)
  implicit def sqlModifyFieldBuilder(a: QueryPath): ModifyFieldBuilder = ModifyFieldBuilder(a)
  implicit def sqlSortBuilder(a: QueryPath): QuerySortBuilder = new QuerySortBuilder(a)

  def deleteFrom(table: QueryPath): DeleteBuilder = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  def update[G[_], A, B](table: QueryPath): UpdateBuilder[G, A, B] = new UpdateBuilder(table, List.empty, QueryComparisonNop[G, B])
  def insertInto(table: QueryPath): InsertBuilder = new InsertBuilder(table)

  def `null`[F[_], A]: QueryValue[F, A] = QueryNull[F, A]
  def  `*`[A]: QueryProjection[A] = QueryProjectAll[A]

  def not[F[_], A, H[_]: Pointed](queryComparison: QueryComparison[F, H[A]])(implicit ev: QueryComparison[F, H[A]] =:= A): H[A] =
    ev(QueryNot[F, H[A]](ev(queryComparison).point[H])).point[H]

  implicit def toQueryValue[F[_], G[_], A, B](a: A)(
    implicit
    $e: Coerce2[F, G, A, B],
    ev2: A =:!= QueryComparison[G, B],
    ev3: F[A]
  ): QueryValue[F] =
    QueryParameter(a)

  implicit def pathToValue[F[_], A](queryPath: QueryPath): QueryValue[F, A] =
    QueryPathValue(queryPath)

  implicit def pathToQueryProjection[F[_], A](queryPath: QueryPath): QueryProjection[QueryValue[F, A]] =
    QueryProjectOne(pathToValue[F, A](queryPath), None)

  implicit def valueToQueryProjection[F[_], A](value: QueryValue[F, A]): QueryProjection[QueryValue[F, A]] =
    QueryProjectOne(value, None)

  implicit def queryBuilderToProjection[F[_], A](builder: QueryBuilder[F]): QueryProjection[A] =
    QueryProjectOne(builder.build, None)
}
