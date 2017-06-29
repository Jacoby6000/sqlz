package scoobie.snacks.mild.sql

import scoobie.ast._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait primitives {
  class QueryValueExtensions[A, B, C](val a: QueryValue[A, B]) {
    import QueryValueComparisonOperator._
    def >(b: QueryValue[A, B]): QueryComparison[C, A] = QueryComparisonValueBinOp(a, b, GreaterThan)
    def >=(b: QueryValue[A, B]): QueryComparison[C, A] = QueryComparisonValueBinOp(a, b, GreaterThanOrEqual)
    def <(b: QueryValue[A, B]): QueryComparison[C, A] = QueryComparisonValueBinOp(a, b, LessThan)
    def <=(b: QueryValue[A, B]): QueryComparison[C, A] = QueryComparisonValueBinOp(a, b, LessThanOrEqual)
    def ===(b: QueryValue[A, B]): QueryComparison[C, A] = QueryComparisonValueBinOp(a, b, Equal)
    def !==(b: QueryValue[A, B]): QueryComparison[C, A] = QueryNot(QueryEqual(a, b))
    def <>(b: QueryValue[A, B]): QueryComparison[C, A] = this !== b

    import QueryValueArithmeticOperator._
    def +(b: QueryValue[A, B]): QueryValue[A, B] = QueryValueBinOp(a, b, Add)
    def -(b: QueryValue[A, B]): QueryValue[A, B] = QueryValueBinOp(a, b, Subtract)
    def /(b: QueryValue[A, B]): QueryValue[A, B] = QueryValueBinOp(a, b, Divide)
    def *(b: QueryValue[A, B]): QueryValue[A, B] = QueryValueBinOp(a, b, Multiply)

    def in(values: QueryValue[A, B]*): QueryComparison[C, A] = QueryIn(a, values.toList)
    def notIn(values: QueryValue[A, B]*): QueryComparison[C, A] = QueryNot(QueryIn(a, values.toList))

    def as(alias: String): QueryProjection[QueryValue[A, B]] = QueryProjectOne(a, Some(alias))
  }

  class SqlQueryFunctionBuilder(val f: QueryPath) {
    def apply[A, B](params: QueryValue[A, B]*): QueryFunction[QueryValue[A, B], B] = QueryFunction(f, params.toList)
  }

  class SqlDslStringInterpolators(val ctx: StringContext) {
    def p(): QueryPath = {
      def go(remainingParts: List[String], queryPath: QueryPath): QueryPath = remainingParts match {
        case head :: tail => go(tail, QueryPathCons(head, queryPath))
        case Nil => queryPath
      }

      val parts = ctx.parts.mkString.split('.').toList.reverse
      go(parts.tail, QueryPathEnd(parts.head))
    }

    def func(): SqlQueryFunctionBuilder = new SqlQueryFunctionBuilder(p())
  }

  class QueryProjectionExtensions[A](val a: QueryProjection[A]) {
    def as(alias: String): QueryProjection[A] = a match {
      case _: QueryProjectAll[A] => a: QueryProjection[A]
      case QueryProjectOne(selection, _) => QueryProjectOne(selection, Some(alias))
    }

    def on[B](comparison: QueryComparison[B, A]): (QueryProjection[A], QueryComparison[B, A]) = (a, comparison)
  }

  class QueryComparisonExtensions[A, B](val left: QueryComparison[A, B]) {
    import QueryComparisonOperator._
    def and(right: QueryComparison[A, B]) = QueryComparisonBinOp(left, right, And)
    def or(right: QueryComparison[A, B]) = QueryComparisonBinOp(left, right, Or)
  }

}
