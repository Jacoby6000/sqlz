package scoobie.snacks.mild.sql.query

import scoobie.ast._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select {
  object SelectBuilderBuilder {
    def apply[F[_]](projections: QueryProjection[F]*): SelectBuilder[F] = SelectBuilder(projections.toList)
  }

  case class SelectBuilder[F[_]](projections: List[QueryProjection[F]]) {
    def from(path: QueryProjection[F]) =
      QueryBuilder(path, projections, List.empty, QueryComparisonNop[F], List.empty, List.empty, None, None)
  }

  case class QueryBuilder[F[_]](
                                 table: QueryProjection[F],
                                 values: List[QueryProjection[F]],
                                 unions: List[QueryJoin[F]],
                                 filter: QueryComparison[F],
                                 sorts: List[QuerySort[F]],
                                 groupings: List[QuerySort[F]],
                                 offset: Option[Int],
                                 limit: Option[Int]
  ) {
    def leftOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(unions = QueryLeftOuterJoin(tup._1, tup._2) :: unions)

    def rightOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(unions = QueryRightOuterJoin(tup._1, tup._2) :: unions)

    def innerJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(unions = QueryInnerJoin(tup._1, tup._2) :: unions)

    def crossJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(unions = QueryCrossJoin(tup._1, tup._2) :: unions)

    def fullOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(unions = QueryFullOuterJoin(tup._1, tup._2) :: unions)

    def where(queryComparison: QueryComparison[F]): QueryBuilder[F] = {
      this.copy(filter = QueryAnd(filter, queryComparison))
    }

    def build: QuerySelect[F] = QuerySelect[F](table, values, unions, filter, sorts, groupings, offset, limit)

    def orderBy(sorts: QuerySort[F]*) = this.copy(sorts = this.sorts ::: sorts.toList)
    def groupBy(groups: QuerySort[F]*) = this.copy(groupings = this.groupings ::: groups.toList)

    def offset(n: Int): QueryBuilder[F] = this.copy(offset = Some(n))
    def limit(n: Int): QueryBuilder[F] = this.copy(limit = Some(n))

  }

  class QuerySortBuilder[F[_]](val f: QueryPath[F]) {
    def asc: QuerySortAsc[F] = QuerySortAsc(f)
    def desc: QuerySortDesc[F] = QuerySortDesc(f)
  }


}
