package scoobie.snacks.mild

import scoobie.ast._
import scoobie.coercion._
import scoobie.cata._

package object sql extends query.modify with query.select with primitives {


  implicit def sqlDslStringInterpolatorConverter[T, A[_]](ctx: StringContext): SqlDslStringInterpolators =
    new SqlDslStringInterpolators(ctx)

  implicit def sqlValueExtensions[T, A[_]](a: A[Indicies.Value])(implicit coerce: Coerce[T, A]): QueryValueExtensions[T, A] =
    new QueryValueExtensions(a)

  implicit def sqlComparisonExtensions[T, A[_]](a: A[Indicies.Comparison])(implicit coerce: Coerce[T, A]): QueryComparisonExtensions[T, A] =
    new QueryComparisonExtensions(a)

  implicit def sqlProjectionExtensions[T, A[_]](a: ProjectOne[T, A]): QueryProjectionExtensions[T, A] =
    new QueryProjectionExtensions(a)

  implicit def sqlModifyFieldBuilder[T, A[_]](a: Path)(implicit coerce: Coerce[T, A]): ModifyFieldBuilder[T, A] =
    ModifyFieldBuilder(a)

  implicit def sqlSortBuilder(a: Path): QuerySortBuilder =
    new QuerySortBuilder(a)

  def deleteFrom[T, A[_]](table: Path): DeleteBuilder[T, A] = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  def update[T, A[_]](table: Path)(implicit lifter: LiftQueryAST[T, A]): QueryUpdate[T, A] =
    QueryUpdate(table, List.empty, lifter.lift(ComparisonNop[T, A]))

  def insertInto[T, A[_]](table: Path): InsertBuilder[T, A] =
    new InsertBuilder(table)

  def `null`[T, A[_]]: QueryValue[T, A] = Null[T, A]
  def  `*`[T, A[_]]: QueryProjection[T, A] = ProjectAll[T, A]


  implicit def toQueryValue[T, A[_], U, F[_]](u: U)(
    implicit
    ev: Coerce[T, A],
    qt: QueryType[F, T],
    fu: F[U]
  ): QueryValue[T, A] =
    Parameter[T, A](qt.toQueryType(u, fu))

  implicit def pathToValue[T, A[_]](queryPath: Path): QueryValue[T, A] =
    PathValue(queryPath)

  implicit def pathToQueryProjection[T, A[_]](queryPath: Path)(implicit lifter: LiftQueryAST[T, A]): QueryProjection[T, A] =
    ProjectOne(lifter.lift(pathToValue[T, A](queryPath)), None)

  implicit def valueToQueryProjection[T, A[_]](value: A[Indicies.Value]): QueryProjection[T, A] =
    ProjectOne(value, None)

  implicit def liftQuery[T, A[_], I](query: Query[T, A, I])(implicit lifter: LiftQueryAST[T, A]): A[I] =
    lifter.lift(query)
}
