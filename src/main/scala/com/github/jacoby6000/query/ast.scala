package com.github.jacoby6000.query
import doobie.imports._
import _root_.shapeless._
import _root_.shapeless.ops.hlist.{Mapper, Prepend, ToTraversable}
import com.github.jacoby6000.query.shapeless.KindConstraint.ConstrainedUnaryTCConstraintTC


/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  private object OfKindContainingHListTC extends ConstrainedUnaryTCConstraintTC[HList] {
    type OfKindContainingHList[TC[_ <: HList]] = {
      type HL[L <: HList] = ConstrainedUnaryTCConstraintTC[L, TC]
    }
  }

  import OfKindContainingHListTC._

  trait UnwrapperPoly[F[_ <: HList]] extends Poly1 {
    implicit def unwrapHValue[A <: HList] = at[F[A]](unwrap)
    def unwrap[A <: HList](f: F[A]): A
  }

  object QueryProjectionUnwrapper extends UnwrapperPoly[QueryProjection] {
    def unwrap[A <: HList](f: QueryProjection[A]): A = f.params
  }

  object QueryUnionUnwrapper extends UnwrapperPoly[QueryUnion] {
    def unwrap[A <: HList](f: QueryUnion[A]): A = f.params
  }

  object QueryComparisonUnwrapper extends UnwrapperPoly[QueryComparison] {
    def unwrap[A <: HList](f: QueryComparison[A]): A = f.params
  }

  object ModifyFieldUnwrapper extends UnwrapperPoly[ModifyField] {
    def unwrap[A <: HList](f: ModifyField[A]): A = f.params
  }

  object QueryValueUnwrapper extends UnwrapperPoly[QueryValue] {
    def unwrap[A <: HList](f: QueryValue[A]): A = f.params
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
    def apply[L <: HList : OfKindContainingHList[QueryValue]#HL, Out <: HList](path: QueryPath, args: L)(implicit toList: ToTraversable.Aux[L, List, QueryValue[_]], m: Mapper.Aux[QueryValueUnwrapper.type, L, Out]): QueryFunction[Out] = QueryFunction[Out](path, toList(args), m(args))
  }

  object QueryIn {
    def apply[A <: HList, B <: HList : OfKindContainingHList[QueryValue]#HL, MappedValues <: HList, Out <: HList](left: QueryValue[A], rights: B)(implicit toList: ToTraversable.Aux[B, List, QueryValue[_]], m: Mapper.Aux[QueryValueUnwrapper.type, B, MappedValues], p: Prepend.Aux[A,MappedValues,Out]): QueryIn[A,Out] = QueryIn[A,Out](left, toList(rights), left.params ::: m(rights))
  }

  sealed trait QueryValue[+L <: HList] { def params: L }
  case class QueryRawExpression[T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[HNil] { lazy val params: HNil = HNil }
  case class QueryParameter[T](value: T) extends QueryValue[T :: HNil] { lazy val params: T :: HNil = value :: HNil}
  case class QueryFunction[L <: HList] private (path: QueryPath, args: List[QueryValue[_]], params: L) extends QueryValue[L]
  case class QueryAdd[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out] { lazy val params: Out = left.params ::: right.params }
  case class QuerySub[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryDiv[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryMul[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryValue[Out] { lazy val params: Out = left.params ::: right.params }
  case object QueryNull extends QueryValue[HNil] { lazy val params: HNil = HNil }

  sealed trait QueryComparison[L <: HList] { def params : L }
  case class QueryEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryNotEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryGreaterThan[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryGreaterThanOrEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryLessThan[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryLessThanOrEqual[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryIn[A <: HList, B <: HList] private (left: QueryValue[A], rights: List[QueryValue[_]], params: B) extends QueryComparison[B]
  case class QueryLit[A <: HList](value: QueryValue[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryNot[A <: HList](value: QueryComparison[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryAnd[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryOr[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }

  sealed trait QueryPath extends QueryValue[HNil] { lazy val params: HNil = HNil }
  case class QueryPathEnd(path: String) extends QueryPath with QueryValue[HNil]
  case class QueryPathCons(path: String, queryPath: QueryPath) extends QueryPath with QueryValue[HNil]

  sealed trait QueryProjection[A <: HList] { def params: A}
  case class QueryProjectOne[A <: HList](selection: QueryValue[A], alias: Option[String]) extends QueryProjection[A] { lazy val params: A = selection.params }
  case object QueryProjectAll extends QueryProjection[HNil] { lazy val params: HNil = HNil }

  sealed trait QueryUnion[A <: HList] { def params: A }
  case class QueryInnerJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out] { lazy val params: Out = table.params ::: on.params }
  case class QueryFullOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out] { lazy val params: Out = table.params ::: on.params }
  case class QueryLeftOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out] { lazy val params: Out = table.params ::: on.params }
  case class QueryRightOuterJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out] { lazy val params: Out = table.params ::: on.params }
  case class QueryCrossJoin[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryUnion[Out] { lazy val params: Out = table.params ::: on.params }

  sealed trait QuerySort
  case class QuerySortAsc(path: QueryPath) extends QuerySort
  case class QuerySortDesc(path: QueryPath) extends QuerySort

  sealed trait QueryExpression[A <: HList] { def params: A }
  sealed trait QueryModify[A <: HList] extends QueryExpression[A]

  object QuerySelect {
    def apply[
            Table <: HList,
            QueryProjections <: HList : OfKindContainingHList[QueryProjection]#HL,
            QueryUnions <: HList : OfKindContainingHList[QueryUnion]#HL,
            ComparisonParameters <: HList,
            MappedValues <: HList,
            MappedUnions <: HList,
            MappedFilters <: HList,
            Out1 <: HList,
            Out2 <: HList,
            Params <: HList
          ](
            table: QueryProjection[Table],
            values: QueryProjections,
            unions: QueryUnions,
            filter: QueryComparison[ComparisonParameters],
            sorts: List[QuerySort],
            groupings: List[QuerySort],
            offset: Option[Int],
            limit: Option[Int]
          )(implicit
            mv: Mapper.Aux[QueryProjectionUnwrapper.type, QueryProjections, MappedValues],
            mu: Mapper.Aux[QueryUnionUnwrapper.type, QueryUnions, MappedUnions],
            p1: Prepend.Aux[Table, MappedValues, Out1],
            p2: Prepend.Aux[Out1, MappedUnions, Out2],
            p3: Prepend.Aux[Out2, ComparisonParameters, Params],
            pl: ToTraversable.Aux[QueryProjections, List, QueryProjection[_ <: HList]],
            ul: ToTraversable.Aux[QueryUnions, List, QueryUnion[_ <: HList]]
          ): QuerySelect[Params] =
              QuerySelect[Params](table, pl(values), ul(unions), filter, sorts, groupings, offset, limit, p3(p2(p1(table.params,mv(values)),mu(unions)), filter.params))
  }

  case class QuerySelect[Params <: HList] private (
                          table: QueryProjection[_ <: HList],
                          values: List[QueryProjection[_ <: HList]],
                          unions: List[QueryUnion[_ <: HList]],
                          filter: QueryComparison[_ <: HList],
                          sorts: List[QuerySort],
                          groupings: List[QuerySort],
                          offset: Option[Int],
                          limit: Option[Int],
                          params: Params
                        ) extends QueryExpression[Params] with QueryValue[Params]

  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A]) { lazy val params: A = value.params}
//  case class QueryInsert[A <: HList : OfKindContainingHList[ModifyField]#HL, Out <: HList]
//                        (collection: QueryPath, values: A)
//                        (implicit m: Mapper.Aux[ModifyFieldUnwrapper.type, A, Out]) extends QueryExpression[Out] with QueryModify[Out] {
//    lazy val params: Out = m(values)
//  }

//  case class QueryUpdate[A <: HList : OfKind[ModifyField]#HL, B <: HList](collection: QueryPath, values: A, where: Option[B]) extends QueryExpression[B] with QueryModify[B]
//  case class QueryDelete[B <:(collection: QueryPath, where: QueryComparison) extends QueryExpression

}
