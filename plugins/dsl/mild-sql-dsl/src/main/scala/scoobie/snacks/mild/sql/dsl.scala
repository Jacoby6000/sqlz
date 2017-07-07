package scoobie.snacks.mild.sql

import scoobie.ast._
import scoobie.coercion._
import scoobie.cata._
import scoobie.snacks.TypeComparisons._

trait dsl[T, A[_]] extends query.modify[T, A] with query.select[T, A] with primitives[T, A] {

  def lifter: LiftQueryAST[T, A]

  implicit def sqlDslStringInterpolatorConverter(ctx: StringContext): SqlDslStringInterpolators =
    new SqlDslStringInterpolators(ctx)

  implicit def sqlValueExtensionsA(a: A[Indicies.Value]): QueryValueExtensions =
    new QueryValueExtensions(a)

  implicit def sqlValueExtensions(a: QueryValue[T, A]): QueryValueExtensions =
    new QueryValueExtensions(lifter.lift(a))

  implicit def pathToSqlValueExtensions(a: Path): QueryValueExtensions =
    new QueryValueExtensions(pathToValue(a))

  implicit def sqlComparisonExtensionsA(a: A[Indicies.Comparison]): QueryComparisonExtensions =
    new QueryComparisonExtensions(a)

  implicit def pathToSqlComparisonExtensionsA(a: Path): QueryComparisonExtensions =
    new QueryComparisonExtensions(pathToComparison(a))

  implicit def sqlComparisonExtensions(a: QueryComparison[T, A]): QueryComparisonExtensions =
    new QueryComparisonExtensions(lifter.lift(a))

  implicit def sqlProjectionExtensionsA(a: A[Indicies.ProjectOneI]): QueryProjectionExtensions =
    new QueryProjectionExtensions(a)

  implicit def sqlProjectOneExtensions(a: ProjectOne[T, A]): QueryProjectionExtensions =
    new QueryProjectionExtensions(lifter.lift(a))

  implicit def sqlProjectAliasExtensions(a: ProjectAlias[T, A]): QueryProjectionExtensions =
    new QueryProjectionExtensions(lifter.lift(a))

  implicit def sqlModifyFieldBuilder(a: Path): ModifyFieldBuilder =
    ModifyFieldBuilder(a)

  implicit def sqlSortBuilder(a: Path): QuerySortBuilder =
    new QuerySortBuilder(a)

  def deleteFrom(table: Path): DeleteBuilder = new DeleteBuilder(table)

  val select = SelectBuilderBuilder

  def update(table: Path): QueryUpdate[T, A] =
    QueryUpdate[T, A](table, List.empty, lifter.lift(ComparisonNop[T, A]))

  def insertInto(table: Path): InsertBuilder =
    new InsertBuilder(table)

  def `null`: QueryValue[T, A] = Null[T, A]
  def  `*`: QueryProjection[T, A] = ProjectAll[T, A]


  implicit def toQueryValue[U, F[_]](u: U)(
    implicit
    ev2: U =:!= Path,
    qt: QueryType[F, T],
    fu: F[U]
  ): A[Indicies.Value] =
    lifter.lift(Parameter[T, A](qt.toQueryType(u, fu)))

  implicit def pathToValue(queryPath: Path): A[Indicies.Value] =
    lifter.lift(PathValue(queryPath))

  implicit def pathToComparison(queryPath: Path): A[Indicies.Comparison] =
    lifter.lift(Lit(pathToValue(queryPath)))

  implicit def pathToQueryProjectOneI(queryPath: Path): A[Indicies.ProjectOneI] =
    lifter.lift(ProjectOne(pathToValue(queryPath)))

  implicit def pathToQueryProjection(queryPath: Path): A[Indicies.Projection] =
    lifter.lift(ProjectOne(pathToValue(queryPath)))

  implicit def valueToQueryProjectOneI(value: A[Indicies.Value]): A[Indicies.ProjectOneI] =
    lifter.lift(ProjectOne(value))

  implicit def valueToQueryProjection(value: A[Indicies.Value]): A[Indicies.Projection] =
    lifter.lift(ProjectOne(value))

  implicit def liftQueryValue(query: QueryValue[T, A]): A[Indicies.Value] =
    lifter.lift(query)

  implicit def liftQueryValueToProjectOneI(query: QueryValue[T, A]): A[Indicies.ProjectOneI] =
    valueToQueryProjectOneI(lifter.lift(query))

  implicit def liftQueryValueToProjection(query: QueryValue[T, A]): A[Indicies.Projection] =
    valueToQueryProjection(lifter.lift(query))

  implicit def liftQueryComparison(query: QueryComparison[T, A]): A[Indicies.Comparison] =
    lifter.lift(query)
}
