package com.github.jacoby6000.query.dsl

import com.github.jacoby6000.query.ast._
import shapeless.HList

/**
  * Created by jacob.barber on 3/4/16.
  */
object sql {


  implicit class StringContextExtensions(c: StringContext) {
    def p(): QueryPath = {
      def go(remainingParts: List[String], queryPath: QueryPath): QueryPath = remainingParts match {
        case head :: tail => go(tail, QueryPathCons(head, queryPath))
        case Nil => queryPath
      }

      val parts = c.parts.mkString.split('.').toList.reverse
      go(parts.tail, QueryPathEnd(parts.head))
    }
  }

  implicit def stringToProjection(f: String): QueryProjectOne = QueryProjectOne(f, None)
  implicit def stringToPathEnd(f: String): QueryPathEnd = QueryPathEnd(f)

  implicit class QueryValueExtensions(f: QueryValue) {
    def as(alias: String) = QueryProjectOne(f, Some(alias))
  }

  implicit class QueryPathExtensions(f: QueryPath) {
    def asc: QuerySortAsc = QuerySortAsc(f)
    def desc: QuerySortDesc = QuerySortDesc(f)
  }

  val `*` = QueryProjectAll
  val `?` = QueryParameter

  def select(projections: QueryProjection*): SelectBuilder = new SelectBuilder(projections.toList)

  case class SelectBuilder(projections: List[QueryProjection]) {
    def from(path: QueryPath): Query = Query(path, projections, List.empty, None, List.empty, List.empty)
  }

  implicit class QueryExtensions(query: Query) {
    def leftOuterJoin(table: QueryPath): JoinBuilder = new JoinBuilder(query, QueryLeftOuterJoin(table, _))
    def rightOuterJoin(table: QueryPath): JoinBuilder = new JoinBuilder(query, QueryRightOuterJoin(table, _))
    def innerJoin(table: QueryPath): JoinBuilder = new JoinBuilder(query, QueryInnerJoin(table, _))
    def fullOuterJoin(table: QueryPath): JoinBuilder = new JoinBuilder(query, QueryFullOuterJoin(table, _))
    def crossJoin(table: QueryPath): JoinBuilder = new JoinBuilder(query, QueryCrossJoin(table, _))

    def where(comparison: QueryComparison): Query = query.copy(filters = Some(comparison))
    def orderBy(sorts: QuerySort*): Query = query.copy(sorts = sorts.toList)
    def groupBy(groups: QuerySort*): Query = query.copy(groupings = groups.toList)
  }

  case class JoinBuilder(query: Query, building: QueryComparison => QueryUnion) {
    def on(comp: QueryComparison): Query = query.copy(unions = query.unions ::: List(building(comp)))
  }

  implicit def queryValueFromableToQueryValue[A](a: A)(implicit arg0: QueryValueFrom[A]): QueryValue = arg0.toQueryValue(a)
  implicit def queryValueFromableToQueryComparison[A](a: A)(implicit arg0: QueryValueFrom[A]): QueryComparison = QueryLit(arg0.toQueryValue(a))

  implicit class QueryValueFromExtensions[A: QueryValueFrom](a: A) {
    def gt[B: QueryValueFrom](b: B) = QueryGreaterThan(a, b)
    def gte[B: QueryValueFrom](b: B) = QueryGreaterThanOrEqual(a, b)
    def lt[B: QueryValueFrom](b: B) = QueryLessThan(a, b)
    def lte[B: QueryValueFrom](b: B) = QueryLessThanOrEqual(a, b)

    def eeq[B: QueryValueFrom](b: B) = QueryEqual(a, b)
    def neq[B: QueryValueFrom](b: B) = QueryNotEqual(a, b)

    def ++[B: QueryValueFrom](b: B) = QueryAdd(a,b)
    def --[B: QueryValueFrom](b: B) = QuerySub(a,b)
    def /[B: QueryValueFrom](b: B) = QueryDiv(a,b)
    def **[B: QueryValueFrom](b: B) = QueryMul(a,b)
  }

  def !(queryComparison: QueryComparison): QueryNot = queryComparison.not

  implicit class QueryComparisonExtensions(left: QueryComparison) {
    def not: QueryNot = QueryNot(left)
    def and(right: QueryComparison) = QueryAnd(left, right)
    def or(right: QueryComparison) = QueryOr(left, right)
  }

  trait QueryValueFrom[A] {
    def toQueryValue(a: A): QueryValue
  }

  object QueryValueFrom {
    def apply[A](f: A => QueryValue) =
      new QueryValueFrom[A] {
        def toQueryValue(a: A): QueryValue = f(a)
      }
  }

  implicit val queryParam = QueryValueFrom[QueryParameter.type](identity)
  implicit val queryStringValue = QueryValueFrom[String](QueryString)
  implicit val queryBooleanValue = QueryValueFrom[Boolean](QueryBoolean)
  implicit val queryIntValue = QueryValueFrom[Int](QueryInt)
  implicit val queryDoubleValue = QueryValueFrom[Double](QueryDouble)
  implicit val queryPath = QueryValueFrom[QueryPath] {
    case cons: QueryPathCons => cons
    case end: QueryPathEnd => end
  }
  implicit val queryPathCons = QueryValueFrom[QueryPathCons](identity)
  implicit val queryPathEnd = QueryValueFrom[QueryPathEnd](identity)
}
