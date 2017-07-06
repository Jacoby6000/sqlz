package scoobie.snacks.mild.sql.query

import scoobie.ast._
import scoobie.cata._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select {
  object SelectBuilderBuilder {
    def apply[T, A[_]](projections: A[Indicies.Projection]*): SelectBuilder[T, A] = SelectBuilder(projections.toList)
  }

  case class SelectBuilder[T, A[_]](projections: List[A[Indicies.Projection]]) {
    def from(path: A[Indicies.ProjectOneI])(implicit lifter: LiftQueryAST[T, A]) =
      QuerySelect[T, A](path, projections, List.empty, lifter.lift(ComparisonNop[T, A]), List.empty, List.empty, None, None)
  }

  class QuerySelectExtensions[T, A[_]](select: QuerySelect[T, A]) {
    import JoinOperators._
    def leftOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder[T, A] =
      JoinBuilder[T, A](select, QueryJoin(projection, _: A[Indicies.Comparison], LeftOuter))

    def rightOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder[T, A] =
      JoinBuilder[T, A](select, QueryJoin(projection, _: A[Indicies.Comparison], RightOuter))

    def innerJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder[T, A] =
      JoinBuilder[T, A](select, QueryJoin(projection, _: A[Indicies.Comparison], Inner))

    def crossJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder[T, A] =
      JoinBuilder[T, A](select, QueryJoin(projection, _: A[Indicies.Comparison], Cartesian))

    def fullOuterJoin(projection: A[Indicies.ProjectOneI]): JoinBuilder[T, A] =
      JoinBuilder[T, A](select, QueryJoin(projection, _: A[Indicies.Comparison], FullOuter))

    def where(queryComparison: A[Indicies.Comparison])(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] = {
      select.copy(filter = lifter.lift(ComparisonBinOp(select.filter, queryComparison, ComparisonOperators.And)))
    }

    def leftOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison]))(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, LeftOuter)))

    def rightOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison]))(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, RightOuter)))

    def innerJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison]))(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, Inner)))

    def crossJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison]))(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, Cartesian)))

    def fullOuterJoin(tup: (A[Indicies.ProjectOneI], A[Indicies.Comparison]))(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ lifter.lift(QueryJoin(tup._1, tup._2, FullOuter)))

    def orderBy(sorts: Sort*) = select.copy(sorts = select.sorts ++ sorts.toList)
    def groupBy(groups: Sort*) = select.copy(groupings = select.groupings ++ groups.toList)

    def offset(n: Long): QuerySelect[T, A] = select.copy(offset = Some(n))
    def limit(n: Long): QuerySelect[T, A] = select.copy(limit = Some(n))

    def as(alias: String)(implicit lifter: LiftQueryAST[T, A]): ProjectOne[T, A] = ProjectOne(lifter.lift(select), Some(alias))
  }

  case class JoinBuilder[T, A[_]](query: QuerySelect[T, A], f: A[Indicies.Comparison] => QueryJoin[T, A]) {
    def on(where: A[Indicies.Comparison])(implicit lifter: LiftQueryAST[T, A]): QuerySelect[T, A] =
      query.copy(joins = query.joins :+ lifter.lift(f(where)))
  }

  class QuerySortBuilder(val f: Path) {
    def asc: Sort = Sort(f, SortType.Ascending)
    def desc: Sort = Sort(f, SortType.Descending)
  }

}
