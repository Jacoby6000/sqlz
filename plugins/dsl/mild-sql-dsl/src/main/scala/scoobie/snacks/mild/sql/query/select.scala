package scoobie.snacks.mild.sql.query

import scoobie.ast._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select {
  object SelectBuilderBuilder {
    def apply[A](projections: QueryProjection[A]*): SelectBuilder[A] = SelectBuilder(projections.toVector)
  }

  case class SelectBuilder[A](projections: Vector[QueryProjection[A]]) {
    def from[B, C](path: QueryProjection[A])(implicit coercion: Coerce[A, B, C]) =
      QueryBuilder[A, B, C](path, projections, Vector.empty, QueryComparisonNop[C, A], Vector.empty, Vector.empty, None, None)
  }

  case class QueryBuilder[A, B, C](
                                 table: QueryProjection[A],
                                 values: Vector[QueryProjection[A]],
                                 joins: Vector[QueryJoin[A, C]],
                                 filter: QueryComparison[C, A],
                                 sorts: Vector[QuerySort],
                                 groupings: Vector[QuerySort],
                                 offset: Option[Long],
                                 limit: Option[Long]
  ) {
    import JoinOp._
    def leftOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(this, QueryJoin(projection, _, LeftOuter))

    def rightOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(this, QueryJoin(projection, _, RightOuter))

    def innerJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(this, QueryJoin(projection, _, Inner))

    def crossJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(this, QueryJoin(projection, _, Cartesian))

    def fullOuterJoin(projection: QueryProjection[A]): JoinBuilder[A, B, C] =
      JoinBuilder(this, QueryJoin(projection, _, FullOuter))

    def where(queryComparison: QueryComparison[C, A]): QueryBuilder[A, B, QueryComparison[C, A]] = {
      this.copy(filter = QueryComparisonBinOp(filter, queryComparison, QueryComparisonOperator.And))
    }

    def leftOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QueryBuilder[A, B, C] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, LeftOuter))

    def rightOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QueryBuilder[A, B, C] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, RightOuter))

    def innerJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QueryBuilder[A, B, C] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, Inner))

    def crossJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QueryBuilder[A, B, C] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, Cartesian))

    def fullOuterJoin(tup: (QueryProjection[A], QueryComparison[C, A])): QueryBuilder[A, B, C] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, FullOuter))

    def build: QuerySelect[A, B, C] =
      QuerySelect(table, values.toList, joins.toList, filter, sorts.toList, groupings.toList, offset, limit)

    def orderBy(sorts: QuerySort*) = this.copy(sorts = this.sorts ++ sorts.toVector)
    def groupBy(groups: QuerySort*) = this.copy(groupings = this.groupings ++ groups.toVector)

    def offset(n: Long): QueryBuilder[A, B, C] = this.copy(offset = Some(n))
    def limit(n: Long): QueryBuilder[A, B, C] = this.copy(limit = Some(n))

    def as(alias: String): QueryProjection[QueryValue[A, B]] = QueryProjectOne(this.build, Some(alias))
  }

  case class JoinBuilder[A, B, C](queryBuilder: QueryBuilder[A, B, C], f: QueryComparison[C, A] => QueryJoin[A, C]) {
    def on(where: QueryComparison[C, A]): QueryBuilder[A, B, C] = queryBuilder.copy(joins = queryBuilder.joins :+ f(where))
  }

  class QuerySortBuilder(val f: QueryPath) {
    def asc: QuerySort = QuerySort(f, SortType.Ascending)
    def desc: QuerySort = QuerySort(f, SortType.Descending)
  }


}
