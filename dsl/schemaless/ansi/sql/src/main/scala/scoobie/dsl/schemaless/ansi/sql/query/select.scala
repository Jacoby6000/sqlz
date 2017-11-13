package scoobie.dsl.schemaless.ansi.sql.query

import scoobie.ast.ansi._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait select {
  object SelectBuilderBuilder {
    def apply[F[_]](projections: QueryProjection[F]*): SelectBuilder[F] = SelectBuilder(projections.toVector)
  }

  case class SelectBuilder[F[_]](projections: Vector[QueryProjection[F]]) {
    def from(path: QueryProjection[F]) =
      QueryBuilder(path, projections, Vector.empty, QueryComparisonNop[F], Vector.empty, Vector.empty, None, None)
  }

  case class QueryBuilder[F[_]](
                                 table: QueryProjection[F],
                                 values: Vector[QueryProjection[F]],
                                 joins: Vector[QueryJoin[F]],
                                 filter: QueryComparison[F],
                                 sorts: Vector[QuerySort[F]],
                                 groupings: Vector[QuerySort[F]],
                                 offset: Option[Long],
                                 limit: Option[Long]
  ) {
    def leftOuterJoin(projection: QueryProjection[F]): JoinBuilder[F] =
      JoinBuilder(this, QueryLeftOuterJoin(projection, _))

    def rightOuterJoin(projection: QueryProjection[F]): JoinBuilder[F] =
      JoinBuilder(this, QueryRightOuterJoin(projection, _))

    def innerJoin(projection: QueryProjection[F]): JoinBuilder[F] =
      JoinBuilder(this, QueryInnerJoin(projection, _))

    def crossJoin(projection: QueryProjection[F]): JoinBuilder[F] =
      JoinBuilder(this, QueryCrossJoin(projection, _))

    def fullOuterJoin(projection: QueryProjection[F]): JoinBuilder[F] =
      JoinBuilder(this, QueryFullOuterJoin(projection, _))

    def where(queryComparison: QueryComparison[F]): QueryBuilder[F] = {
      this.copy(filter = QueryAnd(filter, queryComparison))
    }

    def leftOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(joins = joins :+ QueryLeftOuterJoin(tup._1, tup._2))

    def rightOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(joins = joins :+ QueryRightOuterJoin(tup._1, tup._2))

    def innerJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(joins = joins :+ QueryInnerJoin(tup._1, tup._2))

    def crossJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(joins = joins :+ QueryCrossJoin(tup._1, tup._2))

    def fullOuterJoin(tup: (QueryProjection[F], QueryComparison[F])): QueryBuilder[F] =
      this.copy(joins = joins :+ QueryFullOuterJoin(tup._1, tup._2))

    def build: QuerySelect[F] =
      QuerySelect[F](table, values.toList, joins.toList, filter, sorts.toList, groupings.toList, offset, limit)

    def orderBy(sorts: QuerySort[F]*) = this.copy(sorts = this.sorts ++ sorts.toVector)
    def groupBy(groups: QuerySort[F]*) = this.copy(groupings = this.groupings ++ groups.toVector)

    def offset(n: Long): QueryBuilder[F] = this.copy(offset = Some(n))
    def limit(n: Long): QueryBuilder[F] = this.copy(limit = Some(n))

    def as(alias: String): QueryProjection[F] = QueryProjectOne(this.build, Some(alias))
  }

  case class JoinBuilder[F[_]](queryBuilder: QueryBuilder[F], f: QueryComparison[F] => QueryJoin[F]) {
    def on(where: QueryComparison[F]): QueryBuilder[F] = queryBuilder.copy(joins = queryBuilder.joins :+ f(where))
  }

  class QuerySortBuilder[F[_]](val f: QueryPath[F]) {
    def asc: QuerySortAsc[F] = QuerySortAsc(f)
    def desc: QuerySortDesc[F] = QuerySortDesc(f)
  }


}
