package com.github.jacoby6000.query
import shapeless._
import shapeless.ops.hlist.Prepend


/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  type OfKind[TC[_]] = {
    type HL[L <: HList] = UnaryTCConstraint[L, TC]
  }

  trait RawExpressionHandler[A] {
    def interpret(a: A): String
  }

  object RawExpressionHandler {
    def apply[A](f: A => String): RawExpressionHandler[A] = new RawExpressionHandler[A] {
      def interpret(a: A): String = f(a)
    }
  }

  object QueryFunction {
    def apply[A](path: QueryPath, arg1: A): QueryFunction[A :: HNil] = QueryFunction(path, arg1 :: HNil)
    def apply[A, B](path: QueryPath, arg1: A, arg2: B): QueryFunction[A :: B :: HNil] = QueryFunction(path, arg1 :: arg2 :: HNil)
    def apply[A, B, C](path: QueryPath, arg1: A, arg2: B, arg3: C): QueryFunction[A :: B :: C :: HNil] = QueryFunction(path, arg1 :: arg2 :: arg3 :: HNil)
    def apply[A, B, C, D](path: QueryPath, arg1: A, arg2: B, arg3: C, arg4: D): QueryFunction[A :: B :: C :: D :: HNil] = QueryFunction(path, arg1 :: arg2 :: arg3 :: arg4 :: HNil)
  }

  sealed trait QueryValue[L <: HList]
  case class QueryRawExpression[T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[HNil]
  case class QueryParameter[T](value: T) extends QueryValue[T :: HNil]
  case class QueryFunction[L <: HList](path: QueryPath, args: L) extends QueryValue[L]
  case class QueryAdd[A <: HList, B <: HList, Out <: HList](left: QueryValue, right: QueryValue)(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out]
  case class QuerySub[A <: HList, B <: HList, Out <: HList](left: QueryValue, right: QueryValue)(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out]
  case class QueryDiv[A <: HList, B <: HList, Out <: HList](left: QueryValue, right: QueryValue)(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out]
  case class QueryMul[A <: HList, B <: HList, Out <: HList](left: QueryValue, right: QueryValue)(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out]
  case object QueryNull extends QueryValue[HNil]

  sealed trait QueryComparison[L <: HList]
  case class QueryEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryNotEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryGreaterThan[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryGreaterThanOrEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryLessThan[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryLessThanOrEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
//  case class QueryIn[A <: HList, B <: HList : OfKind[QueryValue]#HL](left: QueryValue[A], rights: B) extends QueryComparison
  case class QueryLit[A <: HList](value: QueryValue[A]) extends QueryComparison[A]
  case class QueryNot[A <: HList](value: QueryComparison[A]) extends QueryComparison[A]
  case class QueryAnd[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]
  case class QueryOr[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out]

  sealed trait QueryPath extends QueryValue[HNil]
  case class QueryPathEnd(path: String) extends QueryPath with QueryValue[HNil]
  case class QueryPathCons(path: String, queryPath: QueryPath) extends QueryPath with QueryValue[HNil]

  sealed trait QueryProjection[A <: HList]
  case class QueryProjectOne[A <: HList](selection: QueryValue[A], alias: Option[String]) extends QueryProjection[A]
  case object QueryProjectAll extends QueryProjection[HNil]

  sealed trait QueryUnion[A <: HList]
  case class QueryInnerJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out]
  case class QueryFullOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out]
  case class QueryLeftOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out]
  case class QueryRightOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out]
  case class QueryCrossJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out]

  sealed trait QuerySort
  case class QuerySortAsc(path: QueryPath) extends QuerySort
  case class QuerySortDesc(path: QueryPath) extends QuerySort

  sealed trait QueryExpression[A <: HList]
  sealed trait QueryModify[A <: HList] extends QueryExpression[A]

  case class QuerySelect[
                          Projections <: HList,
                          Values <: HList,
                          Unions <: HList,
                          Filters <: HList,
                          Out1 <: HList,
                          Out2 <: HList,
                          Out3 <: HList
                        ](
                          table: QueryProjection,
                          values: List[QueryProjection],
                          unions: List[QueryUnion],
                          filters: Option[QueryComparison],
                          sorts: List[QuerySort],
                          groupings: List[QuerySort],
                          offset: Option[Int],
                          limit: Option[Int]
                        )(implicit
                          p1: Prepend.Aux[Projections, Values, Out1],
                          p2: Prepend.Aux[Out1, Unions, Out2],
                          p3: Prepend.Aux[Out2, Filters, Out3]
                        ) extends QueryExpression[Out3] with QueryValue[Out3]

  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A])
  case class QueryInsert[A <: HList : OfKind[ModifyField]#HL](collection: QueryPath, values: A) extends QueryExpression with QueryModify
  case class QueryUpdate(collection: QueryPath, values: List[ModifyField], where: Option[QueryComparison]) extends QueryExpression with QueryModify
  case class QueryDelete(collection: QueryPath, where: QueryComparison) extends QueryExpression
}
