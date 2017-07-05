package scoobie.snacks.mild.sql

import scoobie.ast._
import scoobie.cata._
import scoobie.coercion._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait primitives {

  def not[T, A[_]](queryComparison: A[Indicies.Comparison])(implicit coercion: Coerce[T, A]): QueryComparison[T, A] =
    QueryNot(queryComparison)

  class QueryValueExtensions[A[_]](val a: A[Indicies.Value])(implicit coercion: Coerce[T, A]) {
    import QueryValueComparisonOperator._
    def >(b: A[Indicies.Value]): QueryComparison[T, A] = QueryComparisonValueBinOp(a, b, GreaterThan)
    def >=(b: A[Indicies.Value]): QueryComparison[T, A]= QueryComparisonValueBinOp(a, b, GreaterThanOrEqual)
    def <(b: A[Indicies.Value]): QueryComparison[T, A] = QueryComparisonValueBinOp(a, b, LessThan)
    def <=(b: A[Indicies.Value]): QueryComparison[T, A] = QueryComparisonValueBinOp(a, b, LessThanOrEqual)
    def ===(b: A[Indicies.Value]): QueryComparison[T, A] = QueryComparisonValueBinOp(a, b, Equal)
    def !==[F[_[_[_], _], _]](b: A[Indicies.Value])(implicit liftH: LiftH[F]): QueryComparison[T, A] =
      QueryNot(liftH.lift(this === b))
    def <>[F[_[_[_], _], _]](b: A[Indicies.Value])(implicit liftH: LiftH[F]): QueryComparison[T, A] = this !== b

    import QueryValueArithmeticOperator._
    def +(b: QueryValue[T, A]): QueryValue[T, A] = QueryValueBinOp(a, b, Add)
    def -(b: QueryValue[T, A]): QueryValue[T, A] = QueryValueBinOp(a, b, Subtract)
    def /(b: QueryValue[T, A]): QueryValue[T, A] = QueryValueBinOp(a, b, Divide)
    def *(b: QueryValue[T, A]): QueryValue[T, A] = QueryValueBinOp(a, b, Multiply)

    def in(values: QueryValue[T, A]*): QueryComparison[C, A] = QueryIn(a, values.toList)
    def notIn(values: QueryValue[T, A]*): QueryComparison[C, A] = QueryNot(QueryIn(a, values.toList))

    def as(alias: String): QueryProjection[QueryValue[T, A]] = QueryProjectOne(a, Some(alias))
  }

  class SqlQueryFunctionBuilder(val f: QueryPath) {
    def apply[A, B](params: QueryValue[T, A]*): QueryFunction[QueryValue[T, A], B] = QueryFunction(f, params.toList)
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
