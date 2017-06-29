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
    def from[F[_], G[_], B](path: QueryProjection[A])(implicit coercion: Coerce[F, G, A, B]) =
      QueryBuilder[F, G, A, B](path, projections, Vector.empty, QueryComparisonNop[G, B], Vector.empty, Vector.empty, None, None)
  }

  case class QueryBuilder[F[_], G[_], A, B](
                                 table: QueryProjection[A],
                                 values: Vector[QueryProjection[A]],
                                 joins: Vector[QueryJoin[G, A, B]],
                                 filter: QueryComparison[G, B],
                                 sorts: Vector[QuerySort],
                                 groupings: Vector[QuerySort],
                                 offset: Option[Long],
                                 limit: Option[Long]
  ) {
    import JoinOp._
    def leftOuterJoin(projection: QueryProjection[A]): JoinBuilder[F, G, A, B] =
      JoinBuilder(this, QueryJoin(projection, _, LeftOuter))

    def rightOuterJoin(projection: QueryProjection[A]): JoinBuilder[F, G, A, B] =
      JoinBuilder(this, QueryJoin(projection, _, RightOuter))

    def innerJoin(projection: QueryProjection[A]): JoinBuilder[F, G, A, B] =
      JoinBuilder(this, QueryJoin(projection, _, Inner))

    def crossJoin(projection: QueryProjection[A]): JoinBuilder[F, G, A, B] =
      JoinBuilder(this, QueryJoin(projection, _, Cartesian))

    def fullOuterJoin(projection: QueryProjection[A]): JoinBuilder[F, G, A, B] =
      JoinBuilder(this, QueryJoin(projection, _, FullOuter))

    def where(queryComparison: QueryComparison[G, B]): QueryBuilder[F, G, A, B] = {
      this.copy(filter = QueryComparisonBinOp(filter, queryComparison, And))
    }

    def leftOuterJoin(tup: (QueryProjection[A], QueryComparison[G, B])): QueryBuilder[F, G, A, B] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, LeftOuter))

    def rightOuterJoin(tup: (QueryProjection[A], QueryComparison[G, B])): QueryBuilder[F, G, A, B] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, RightOuter))

    def innerJoin(tup: (QueryProjection[A], QueryComparison[G, B])): QueryBuilder[F, G, A, B] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, Inner))

    def crossJoin(tup: (QueryProjection[A], QueryComparison[G, B])): QueryBuilder[F, G, A, B] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, Cartesian))

    def fullOuterJoin(tup: (QueryProjection[A], QueryComparison[G, B])): QueryBuilder[F, G, A, B] =
      this.copy(joins = joins :+ QueryJoin(tup._1, tup._2, FullOuter))

    def build: QuerySelect[F, G, A, B] =
      QuerySelect(table, values.toList, joins.toList, filter, sorts.toList, groupings.toList, offset, limit)

    def orderBy(sorts: QuerySort*) = this.copy(sorts = this.sorts ++ sorts.toVector)
    def groupBy(groups: QuerySort*) = this.copy(groupings = this.groupings ++ groups.toVector)

    def offset(n: Long): QueryBuilder[F, G, A, B] = this.copy(offset = Some(n))
    def limit(n: Long): QueryBuilder[F, G, A, B] = this.copy(limit = Some(n))

    def as(alias: String): QueryProjection[QueryValue[F, A]] = QueryProjectOne(this.build, Some(alias))
  }

  case class JoinBuilder[F[_], G[_], A, B](queryBuilder: QueryBuilder[F, G, A, B], f: QueryComparison[G, B] => QueryJoin[G, A, B]) {
    def on(where: QueryComparison[G, B]): QueryBuilder[F, G, A, B] = queryBuilder.copy(joins = queryBuilder.joins :+ f(where))
  }

  class QuerySortBuilder(val f: QueryPath) {
    def asc: QuerySort = QuerySort(f, SortType.Ascending)
    def desc: QuerySort = QuerySort(f, SortType.Descending)
  }


}
