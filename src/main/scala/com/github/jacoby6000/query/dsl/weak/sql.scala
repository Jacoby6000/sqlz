package com.github.jacoby6000.query.dsl.weak

import com.github.jacoby6000.query.ast._

import scala.annotation.implicitNotFound
import scala.util.matching.Regex

/**
  * Created by jacob.barber on 3/4/16.
  */
object sql {

  case class MetaData[A, B](value: A, meta: B)

  object implicitConversions {
    implicit def selectBuilderToSelectQuery(queryBuilder: QueryBuilder): QuerySelect = queryBuilder.query
    implicit def updateBuilderToUpdateQuery(updateBuilder: UpdateBuilder): QueryUpdate = updateBuilder.update
  }
  // Delete DSL helpers
  def deleteFrom(table: QueryPath): DeleteBuilder = DeleteBuilder(table)

  case class DeleteBuilder(table: QueryPath) {
    def where(queryComparison: QueryComparison) = QueryDelete(table, queryComparison)
  }

  // Update DSL helpers
  def update(table: QueryPath): UpdateBuilder = UpdateBuilder(QueryUpdate(table, List.empty, None))

  case class UpdateBuilder(update: QueryUpdate) {
    def set(modifyFields: ModifyField*): UpdateBuilder = UpdateBuilder(update.copy(values = update.values ::: modifyFields.toList))
    def where(where: QueryComparison): QueryUpdate = QueryUpdate(update.collection, update.values, Some(where))
  }

  implicit class QueryPathUpdateExtensions(val queryPath: QueryPath) extends AnyVal {
    def ==>(value: QueryValue): ModifyField = ModifyField(queryPath, value)
  }

  // Insert DSL helpers
  def insertInto(table: QueryPath)(columns: QueryPath*): InsertBuilder = InsertBuilder(table, columns.toList)

  case class InsertBuilder(table: QueryPath, columns: List[QueryPath]) {
    def values(values: QueryValue*): QueryInsert = QueryInsert(table, (columns zip values) map (kv => ModifyField(kv._1, kv._2)))
  }

  // Select/Query DSL helpers
  case class SqlQueryFunctionBuilder(f: QueryPath) {
    def apply(params: QueryValue*) = QueryFunction(f, params.toList)
  }

  implicit class StringContextExtensions(val c: StringContext) extends AnyVal {
    def p(): QueryPath = {
      def go(remainingParts: List[String], queryPath: QueryPath): QueryPath = remainingParts match {
        case head :: tail => go(tail, QueryPathCons(head, queryPath))
        case Nil => queryPath
      }

      val parts = c.parts.mkString.split('.').toList.reverse
      go(parts.tail, QueryPathEnd(parts.head))
    }

    def r(args: Any*): Regex = c.standardInterpolator(identity, args).r

    def expr(args: String*)(implicit ev0: RawExpressionHandler[String]): QueryRawExpression[String] = {
      QueryRawExpression(c.standardInterpolator(identity,args))
    }

    def func(): SqlQueryFunctionBuilder = SqlQueryFunctionBuilder(p())
  }

  r"asdasda"


  implicit class QueryValueExtensions(val f: QueryValue) extends AnyVal {
    def as(alias: String) = QueryProjectOne(f, Some(alias))
  }

  implicit class QueryPathExtensions(val f: QueryPath) extends AnyVal  {
    def as(alias: String) = f match {
      case c: QueryPathCons => QueryProjectOne(c, Some(alias))
      case c: QueryPathEnd => QueryProjectOne(c, Some(alias))
    }

    def asc: QuerySortAsc = QuerySortAsc(f)
    def desc: QuerySortDesc = QuerySortDesc(f)
  }

  val `*` = QueryProjectAll
  val `?` = QueryParameter
  val `null` = QueryNull

  def select(projections: QueryProjection*): SelectBuilder = new SelectBuilder(projections.toList)

  case class SelectBuilder(projections: List[QueryProjection]) {
    def from(path: QueryProjection): QueryBuilder = QueryBuilder(QuerySelect(path, projections, List.empty, None, List.empty, List.empty, None, None))
  }

  case class QueryBuilder(query: QuerySelect) {
    def leftOuterJoin(table: QueryProjectOne): JoinBuilder = new JoinBuilder(query, QueryLeftOuterJoin(table, _))
    def rightOuterJoin(table: QueryProjectOne): JoinBuilder = new JoinBuilder(query, QueryRightOuterJoin(table, _))
    def innerJoin(table: QueryProjectOne): JoinBuilder = new JoinBuilder(query, QueryInnerJoin(table, _))
    def fullOuterJoin(table: QueryProjectOne): JoinBuilder = new JoinBuilder(query, QueryFullOuterJoin(table, _))
    def crossJoin(table: QueryProjectOne): JoinBuilder = new JoinBuilder(query, QueryCrossJoin(table, _))

    def where(comparison: QueryComparison): QueryBuilder = QueryBuilder(query.copy(filters = query.filters.map(_ and comparison) orElse Some(comparison)))
    def orderBy(sorts: QuerySort*): QueryBuilder = QueryBuilder(query.copy(sorts = query.sorts ::: sorts.toList))
    def groupBy(groups: QuerySort*): QueryBuilder = QueryBuilder(query.copy(groupings = query.groupings ::: groups.toList))

    def offset(n: Int): QueryBuilder = QueryBuilder(query.copy(offset = Some(n)))
    def limit(n: Int): QueryBuilder = QueryBuilder(query.copy(limit = Some(n)))
  }

  case class JoinBuilder(query: QuerySelect, building: QueryComparison => QueryUnion) {
    def on(comp: QueryComparison): QueryBuilder = QueryBuilder(query.copy(unions = query.unions ::: List(building(comp))))
  }


  implicit class QueryValueTransformerSqlOps[A](val a: A)(implicit arg0: QueryValueTransformer[A]) {
    def >[B: QueryValueTransformer](b: B) = QueryGreaterThan(a.toQueryValue, b.toQueryValue)
    def >=[B: QueryValueTransformer](b: B) = QueryGreaterThanOrEqual(a.toQueryValue, b.toQueryValue)
    def <[B: QueryValueTransformer](b: B) = QueryLessThan(a.toQueryValue, b.toQueryValue)
    def <=[B: QueryValueTransformer](b: B) = QueryLessThanOrEqual(a.toQueryValue, b.toQueryValue)

    def ===[B: QueryValueTransformer](b: B) = QueryEqual(a.toQueryValue, b.toQueryValue)
    def !==[B: QueryValueTransformer](b: B) = QueryNotEqual(a.toQueryValue, b.toQueryValue)

    def ++[B: QueryValueTransformer](b: B) = QueryAdd(a.toQueryValue,b.toQueryValue)
    def --[B: QueryValueTransformer](b: B) = QuerySub(a.toQueryValue,b.toQueryValue)
    def /[B: QueryValueTransformer](b: B) = QueryDiv(a.toQueryValue,b.toQueryValue)
    def **[B: QueryValueTransformer](b: B) = QueryMul(a.toQueryValue,b.toQueryValue)

    def in[B: QueryValueTransformer](b: B*) = QueryIn(a.toQueryValue, b.map(_.toQueryValue).toList)

    def toQueryValue: QueryValue = arg0.toQueryValue(a)

  }

  def !(queryComparison: QueryComparison): QueryNot = queryComparison.not

  implicit class QueryComparisonExtensions(val left: QueryComparison) extends AnyVal {
    def not: QueryNot = QueryNot(left)
    def and(right: QueryComparison) = QueryAnd(left, right)
    def or(right: QueryComparison) = QueryOr(left, right)
  }

  @implicitNotFound("Could not find implicit QueryValueTransformer[${A}]. Make sure you have imported package com.github.jacoby6000.query.dsl.sql._, and that the typed provided has a QueryValueTransformer in scope.")
  trait QueryValueTransformer[A] {
    def toQueryValue(a: A): QueryValue
  }

  object QueryValueTransformer {
    def apply[A](f: A => QueryValue) =
      new QueryValueTransformer[A] {
        def toQueryValue(a: A): QueryValue = f(a)
      }
  }

  implicit val queryParamTransformer: QueryValueTransformer[QueryParameter.type] = QueryValueTransformer[QueryParameter.type](identity)
  implicit val queryNullTransformer: QueryValueTransformer[QueryNull.type] = QueryValueTransformer[QueryNull.type](identity)
  implicit val queryBooleanValueTransformer: QueryValueTransformer[Boolean] = QueryValueTransformer[Boolean](QueryBoolean)
  implicit val queryIntValueTransformer: QueryValueTransformer[Int] = QueryValueTransformer[Int](QueryInt)
  implicit val queryDoubleValueTransformer: QueryValueTransformer[Double] = QueryValueTransformer[Double](QueryDouble)
  implicit val queryPathTransformer: QueryValueTransformer[QueryPath] = QueryValueTransformer[QueryPath] {
    case cons: QueryPathCons => cons
    case end: QueryPathEnd => end
  }
  implicit val queryPathConsTransformer: QueryValueTransformer[QueryPathCons] = QueryValueTransformer[QueryPathCons](identity)
  implicit val queryPathEndTransformer: QueryValueTransformer[QueryPathEnd] = QueryValueTransformer[QueryPathEnd](identity)
  implicit val queryStringValueTransformer: QueryValueTransformer[String] = QueryValueTransformer[String](QueryString)
  implicit val queryFunctionTransformer: QueryValueTransformer[QueryFunction] = QueryValueTransformer[QueryFunction](identity)

  implicit def pathToProjection(f: QueryPath): QueryProjection = QueryProjectOne(f.toQueryValue, None)

}
