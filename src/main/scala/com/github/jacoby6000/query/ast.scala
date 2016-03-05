package com.github.jacoby6000.query


/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  sealed trait QueryValue
  case class QueryString(value: String) extends QueryValue
  case class QueryInt(value: Int) extends QueryValue
  case class QueryDouble(value: Double) extends QueryValue
  case class QueryBoolean(value: Boolean) extends QueryValue
  case class QueryFunction(path: QueryPath, args: List[QueryValue]) extends QueryValue
  case class QueryAdd(left: QueryValue, right: QueryValue) extends QueryValue
  case class QuerySub(left: QueryValue, right: QueryValue) extends QueryValue
  case class QueryDiv(left: QueryValue, right: QueryValue) extends QueryValue
  case class QueryMul(left: QueryValue, right: QueryValue) extends QueryValue
  case object QueryParameter extends QueryValue
  case object QueryNull extends QueryValue

  sealed trait QueryComparison
  case class QueryEqual(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryNotEqual(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryGreaterThan(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryGreaterThanOrEqual(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryLessThan(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryLessThanOrEqual(left: QueryValue, right: QueryValue) extends QueryComparison
  case class QueryLit(value: QueryValue) extends QueryComparison
  case class QueryNot(value: QueryComparison) extends QueryComparison
  case class QueryAnd(left: QueryComparison, right: QueryComparison) extends QueryComparison
  case class QueryOr(left: QueryComparison, right: QueryComparison) extends QueryComparison

  sealed trait QueryPath
  case class QueryPathEnd(path: String) extends QueryPath with QueryValue
  case class QueryPathCons(path: String, queryPath: QueryPath) extends QueryPath with QueryValue

  sealed trait QueryProjection
  case class QueryProjectOne(selection: QueryValue, alias: Option[String]) extends QueryProjection
  case object QueryProjectAll extends QueryProjection

  sealed trait QueryUnion
  case class QueryInnerJoin(table: QueryPath, on: QueryComparison) extends QueryUnion
  case class QueryFullOuterJoin(table: QueryPath, on: QueryComparison) extends QueryUnion
  case class QueryLeftOuterJoin(table: QueryPath, on: QueryComparison) extends QueryUnion
  case class QueryRightOuterJoin(table: QueryPath, on: QueryComparison) extends QueryUnion
  case class QueryCrossJoin(table: QueryPath, on: QueryComparison) extends QueryUnion

  sealed trait QuerySort
  case class QuerySortAsc(path: QueryPath) extends QuerySort
  case class QuerySortDesc(path: QueryPath) extends QuerySort

  case class Query(
                    table: QueryPath,
                    values: List[QueryProjection],
                    unions: List[QueryUnion],
                    filters: Option[QueryComparison],
                    sorts: List[QuerySort],
                    groupings: List[QuerySort])

  case class InsertField(key: QueryPath, value: QueryValue)
  case class Insert(collection: QueryPath, values: List[InsertField])
  case class Update(collection: QueryPath, values: List[InsertField], where: Option[QueryComparison])
  case class Delete(collection: QueryPath, where: QueryComparison)
}
