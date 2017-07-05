package scoobie.snacks.mild.sql.query

import scoobie.ast._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select {
  object SelectBuilderBuilder {
    def apply[T, A[_]](projections: QueryProjection[T, A]*): SelectBuilder[T, A] = SelectBuilder(projections.toList)
  }

  case class SelectBuilder[T, A[_]](projections: List[QueryProjection[T, A]]) {
    def from(path: QueryProjectOne[T, A]) =
      QuerySelect[T, A](path, projections, List.empty, QueryComparisonNop[T, A], List.empty, List.empty, None, None)
  }

  class QuerySelectExtensions[T, A[_]](select: QuerySelect[T, A]) {
    import JoinOp._
    def leftOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(select, QueryJoin(projection, _, LeftOuter))

    def rightOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(select, QueryJoin(projection, _, RightOuter))

    def innerJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(select, QueryJoin(projection, _, Inner))

    def crossJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(select, QueryJoin(projection, _, Cartesian))

    def fullOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(select, QueryJoin(projection, _, FullOuter))

    def where(queryComparison: QueryComparison[C, A]): QuerySelect[T, A] = {
      select.copy(filter = QueryComparisonBinOp(select.filter, queryComparison, QueryComparisonOperator.And))
    }

    def leftOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ QueryJoin(tup._1, tup._2, LeftOuter))

    def rightOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ QueryJoin(tup._1, tup._2, RightOuter))

    def innerJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ QueryJoin(tup._1, tup._2, Inner))

    def crossJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ QueryJoin(tup._1, tup._2, Cartesian))

    def fullOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QuerySelect[T, A] =
      select.copy(joins = select.joins :+ QueryJoin(tup._1, tup._2, FullOuter))

    def orderBy(sorts: QuerySort*) = select.copy(sorts = select.sorts ++ sorts.toList)
    def groupBy(groups: QuerySort*) = select.copy(groupings = select.groupings ++ groups.toList)

    def offset(n: Long): QuerySelect[T, A] = select.copy(offset = Some(n))
    def limit(n: Long): QuerySelect[T, A] = select.copy(limit = Some(n))

    def as(alias: String): QueryProjection[QueryValue[A, B]] = QueryProjectOne(select.build, Some(alias))
  }

  case class JoinBuilder[T, A[_]](query: QuerySelect[T, A], f: A[Indicies.Comparison] => A[Indicies.Join]) {
    def on(where: A[Indicies.Comparison]): QuerySelect[T, A] = query.copy(joins = query.joins :+ f(where))
  }

  class QuerySortBuilder(val f: QueryPath) {
    def asc: QuerySort = QuerySort(f, SortType.Ascending)
    def desc: QuerySort = QuerySort(f, SortType.Descending)
  }


}
