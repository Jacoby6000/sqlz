package scoobie.snacks.mild.sql

import scoobie.ast._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait primitives {
  class QueryValueExtensions[F[_], G[_], A, B](val a: QueryValue[F, A]) {
    import QueryValueComparisonOperator._
    def >[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryComparisonValueBinOp(a, b, GreaterThan)
    def >=[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryComparisonValueBinOp(a, b, GreaterThanOrEqual)
    def <[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryComparisonValueBinOp(a, b, LessThan)
    def <=[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryComparisonValueBinOp(a, b, LessThanOrEqual)
    def ===[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryComparisonValueBinOp(a, b, Equal)
    def !==[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = QueryNot(QueryEqual(a, b))
    def <>[G[_], B](b: QueryValue[F, A])(implicit ga: G[A]): QueryComparison[G, B] = this !== b

    import QueryValueArithmeticOperator._
    def +(b: QueryValue[F, A]): QueryValue[F, A] = QueryValueBinOp(a, b, Add)
    def -(b: QueryValue[F, A]): QueryValue[F, A] = QueryValueBinOp(a, b, Subtract)
    def /(b: QueryValue[F, A]): QueryValue[F, A] = QueryValueBinOp(a, b, Divide)
    def *(b: QueryValue[F, A]): QueryValue[F, A] = QueryValueBinOp(a, b, Multiply)

    def in(values: QueryValue[F, A]*)(implicit ga: G[A]): QueryComparison[G, B] = QueryIn(a, values.toList)
    def notIn(values: QueryValue[F, A]*)(implicit ga: G[A]): QueryComparison[G, B] = QueryNot(QueryIn(a, values.toList))

    def as(alias: String): QueryProjection[QueryValue[F, A]] = QueryProjectOne(a, Some(alias))
  }

  class SqlQueryFunctionBuilder(val f: QueryPath) {
    def apply[F[_], A](params: QueryValue[F, A]*): QueryFunction[F, QueryValue[F, A]] = QueryFunction(f, params.toList)
  }

  class SqlDslStringInterpolators[F[_], A](val ctx: StringContext) {
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

    def on[F[_], B](comparison: QueryComparison[F, B]): (QueryProjection[A], QueryComparison[F, B]) = (a, comparison)
  }

  class QueryComparisonExtensions[F[_], A](val left: QueryComparison[F, A]) {
    import QueryComparisonOperator._
    def and(right: QueryComparison[F, A]) = QueryComparisonBinOp(left, right, And)
    def or(right: QueryComparison[F, A]) = QueryComparisonBinOp(left, right, Or)
  }

}
