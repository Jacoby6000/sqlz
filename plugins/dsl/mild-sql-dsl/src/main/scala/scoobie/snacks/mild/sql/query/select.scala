package scoobie.snacks.mild.sql.query

import scoobie.ast._
import scoobie.coercion._
import scoobie.cata._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select[T, A[_]] {

  def lifter: LiftQueryAST[T, A]

  object SelectBuilderBuilder {
    def apply(projections: A[Indicies.Projection]*): SelectBuilder = SelectBuilder(projections.toList)
  }

  case class SelectBuilder(projections: List[A[Indicies.Projection]]) {
    def from(path: A[Indicies.ProjectOneI]) =
      QuerySelect[T, A](path, projections, List.empty, lifter.lift(ComparisonNop[T, A]), List.empty, List.empty, None, None)
  }

  class QuerySelectExtensions(select: QuerySelect[T, A]) {
    import JoinOperators._
    def leftOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder =
      JoinBuilder(select, QueryJoin(projection, _: A[Indicies.Comparison], LeftOuter))

    def rightOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder =
      JoinBuilder(select, QueryJoin(projection, _: A[Indicies.Comparison], RightOuter))

    def innerJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder =
      JoinBuilder(select, QueryJoin(projection, _: A[Indicies.Comparison], Inner))

    def crossJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder =
      JoinBuilder(select, QueryJoin(projection, _: A[Indicies.Comparison], Cartesian))

    def fullOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder =
      JoinBuilder(select, QueryJoin(projection, _: A[Indicies.Comparison], FullOuter))

    def where(queryComparison: A[Indicies.Comparison]): QuerySelect[T, A] = {
      select.copy(filter = lifter.lift(ComparisonBinOp(select.filter, queryComparison, ComparisonOperators.And)))
    }

    def leftOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, LeftOuter)))

    def rightOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, RightOuter)))

    def innerJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, Inner)))

    def crossJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, Cartesian)))

    def fullOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, FullOuter)))

    def orderBy(sorts: Sort*) = select.copy(sorts = select.sorts ++ sorts.toList)
    def groupBy(groups: Sort*) = select.copy(groupings = select.groupings ++ groups.toList)

    def offset(n: Long): QuerySelect[T, A] = select.copy(offset = Some(n))
    def limit(n: Long): QuerySelect[T, A] = select.copy(limit = Some(n))

    def as(alias: String): ProjectAlias[T, A] =
      ProjectAlias(lifter.lift(ProjectOne(lifter.lift(select))), alias)
  }

  case class JoinBuilder(query: QuerySelect[T, A], f: A[Indicies.Comparison] => QueryJoin[T, A]) {
    def on(where: A[Indicies.Comparison]): QuerySelect[T, A] =
      query.copy(joins = query.joins :+ lifter.lift(f(where)))
  }

  class QuerySortBuilder(val f: Path) {
    def asc: Sort = Sort(f, SortType.Ascending)
    def desc: Sort = Sort(f, SortType.Descending)
  }

}
