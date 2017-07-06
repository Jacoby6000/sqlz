package scoobie.snacks.mild.sql

import scoobie.ast._
import scoobie.cata._
import scoobie.coercion._

/**
  * Created by jacob.barber on 5/24/16.
  */
trait primitives {

  def not[T, A[_]](queryComparison: A[Indicies.Comparison])(implicit coercion: Coerce[T, A]): QueryComparison[T, A] =
    Not(queryComparison)

  class QueryValueExtensions[T, A[_]](val a: A[Indicies.Value])(implicit coercion: Coerce[T, A]) {
    import ComparisonValueOperators._
    def >(b: A[Indicies.Value]): QueryComparison[T, A] = ComparisonValueBinOp(a, b, GreaterThan)
    def >=(b: A[Indicies.Value]): QueryComparison[T, A]= ComparisonValueBinOp(a, b, GreaterThanOrEqual)
    def <(b: A[Indicies.Value]): QueryComparison[T, A] = ComparisonValueBinOp(a, b, LessThan)
    def <=(b: A[Indicies.Value]): QueryComparison[T, A] = ComparisonValueBinOp(a, b, LessThanOrEqual)
    def ===(b: A[Indicies.Value]): QueryComparison[T, A] = ComparisonValueBinOp(a, b, Equal)
    def !==(b: A[Indicies.Value])(implicit lifter: LiftQueryAST[T, A]): QueryComparison[T, A] = not(lifter.lift(this === b))
    def <>(b: A[Indicies.Value])(implicit lifter: LiftQueryAST[T, A]): QueryComparison[T, A] = this !== b

    import ValueOperators._
    def +(b: A[Indicies.Value]): QueryValue[T, A] = ValueBinOp(a, b, Add)
    def -(b: A[Indicies.Value]): QueryValue[T, A] = ValueBinOp(a, b, Subtract)
    def /(b: A[Indicies.Value]): QueryValue[T, A] = ValueBinOp(a, b, Divide)
    def *(b: A[Indicies.Value]): QueryValue[T, A] = ValueBinOp(a, b, Multiply)

    def in(values: A[Indicies.Value]*): QueryComparison[T, A] = In(a, values.toList)
    def notIn(values: A[Indicies.Value]*)(implicit lifter: LiftQueryAST[T, A]): QueryComparison[T, A] =
      not(lifter.lift(this.in(values: _*)))

    def as(alias: String): ProjectOne[T, A] = ProjectOne(a, Some(alias))
  }

  class SqlQueryFunctionBuilder(val f: Path) {
    def apply[T, A[_]](params: A[Indicies.Value]*): QueryValue[T, A] = Function(f, params.toList)
  }

  class SqlDslStringInterpolators(val ctx: StringContext) {
    def p(): Path = {
      def go(remainingParts: List[String], queryPath: Path): Path = remainingParts match {
        case head :: tail => go(tail, PathCons(head, queryPath))
        case Nil => queryPath
      }

      val parts = ctx.parts.mkString.split('.').toList.reverse
      go(parts.tail, PathEnd(parts.head))
    }

    def func(): SqlQueryFunctionBuilder = new SqlQueryFunctionBuilder(p())
  }

  class QueryProjectionExtensions[T, A[_]](val a: ProjectOne[T, A]) {
    def as(alias: String): QueryProjectOne[T, A] = ProjectOne[T, A](a.selection, Some(alias))
    def on[B](comparison: QueryComparison[T, A]): (QueryProjection[T, A], QueryComparison[T, A]) = (a, comparison)
  }

  class QueryComparisonExtensions[T, A[_]](val left: A[Indicies.Comparison]){
    import ComparisonOperators._
    def and(right: A[Indicies.Comparison]) = ComparisonBinOp(left, right, And)
    def or(right: A[Indicies.Comparison]) = ComparisonBinOp(left, right, Or)
  }

}
