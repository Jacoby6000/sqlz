package com.github.jacoby6000.query
import doobie.imports._
import shapeless._
import shapeless.ops.hlist.{Comapped, LeftFolder, Mapped, Mapper, Prepend}


/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  trait OfKindContainingHListTC[L <: HList, TC[_ <: HList]] extends Serializable

  object OfKindContainingHListTC {
    def apply[L <: HList, TC[_ <: HList]](implicit utcc: OfKindContainingHListTC[L, TC]): OfKindContainingHListTC[L, TC] = utcc

    type OfKindContainingHList[TC[_ <: HList]] = {
      type HL[L <: HList] = OfKindContainingHListTC[L, TC]
    }

    implicit def hnilUnaryTC[TC[_ <: HList]] = new OfKindContainingHListTC[HNil, TC] {}
    implicit def hlistUnaryTC1[H <: HList, T <: HList, TC[_ <: HList]](implicit utct : OfKindContainingHListTC[T, TC]) =
      new OfKindContainingHListTC[TC[H] :: T, TC] {}

    implicit def hlistUnaryTC2[L <: HList] = new OfKindContainingHListTC[L, Id] {}

    implicit def hlistUnaryTC3[H] = new OfKindContainingHListTC[HNil, Const[H]#λ] {}
    implicit def hlistUnaryTC4[H, T <: HList](implicit utct : OfKindContainingHListTC[T, Const[H]#λ]) =
      new OfKindContainingHListTC[H :: T, Const[H]#λ] {}
  }

  import OfKindContainingHListTC.OfKindContainingHList



  trait RawExpressionHandler[A] {
    def interpret(a: A): String
  }

  object RawExpressionHandler {
    def apply[A](f: A => String): RawExpressionHandler[A] = new RawExpressionHandler[A] {
      def interpret(a: A): String = f(a)
    }
  }

  sealed trait QueryValue[L <: HList] { def params: L }
  case class QueryRawExpression[T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[HNil] { lazy val params: HNil = HNil }
  case class QueryParameter[T](value: T) extends QueryValue[T :: HNil] { lazy val params: T :: HNil = value :: HNil}
  case class QueryFunction[L <: HList](path: QueryPath, args: L) extends QueryValue[L] { lazy val params: L = args }
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
//  case class QueryIn[A <: HList, B <: HList : OfKind[QueryValue]#HL](left: QueryValue[A], rights: B) extends QueryComparison
  case class QueryLit[A <: HList](value: QueryValue[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryNot[A <: HList](value: QueryComparison[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryAnd[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }
  case class QueryOr[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A,B,Out]) extends QueryComparison[Out] { lazy val params: Out = left.params ::: right.params }

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


  object QuerySelect {
    def apply[
      Table <: HList,
      QueryProjections <: HList : OfKindContainingHList[QueryProjection]#HL,
      QueryUnions <: HList : OfKindContainingHList[QueryUnion]#HL,
      QueryComparisons <: HList,
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
         filter: QueryComparison[QueryComparisons],
         sorts: List[QuerySort],
         groupings: List[QuerySort],
         offset: Option[Int],
         limit: Option[Int]
       )(implicit
         mv: Mapper.Aux[QueryProjectionUnwrapper.type, QueryProjections, MappedValues],
         mu: Mapper.Aux[QueryUnionUnwrapper.type, QueryUnions, MappedUnions],
         p1: Prepend.Aux[Table, MappedValues, Out1],
         p2: Prepend.Aux[Out1, MappedUnions, Out2],
         p3: Prepend.Aux[Out2, QueryComparisons, Params]
       ): QuerySelect[Table, QueryProjections, QueryUnions, QueryComparisons, Params] = QuerySelect(table, values, unions, filter, sorts, groupings, offset, limit, p3(p2(p1(table.params,mv(values)),mu(unions)), filter.params))
  }

  case class QuerySelect[
                          Table <: HList,
                          QueryProjections <: HList : OfKindContainingHList[QueryProjection]#HL,
                          QueryUnions <: HList : OfKindContainingHList[QueryUnion]#HL,
                          QueryComparisons <: HList,
                          Params <: HList
                        ] private (
                          table: QueryProjection[Table],
                          values: QueryProjections,
                          unions: QueryUnions,
                          filter: QueryComparison[QueryComparisons],
                          sorts: List[QuerySort],
                          groupings: List[QuerySort],
                          offset: Option[Int],
                          limit: Option[Int],
                          params: Params
                        ) extends QueryExpression[Params]

  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A]) { lazy val params: A = value.params}
  case class QueryInsert[A <: HList : OfKindContainingHList[ModifyField]#HL, Out <: HList]
                        (collection: QueryPath, values: A)
                        (implicit m: Mapper.Aux[ModifyFieldUnwrapper.type, A, Out]) extends QueryExpression[Out] with QueryModify[Out] {
    lazy val params: Out = m(values)
  }
//  case class QueryUpdate[A <: HList : OfKind[ModifyField]#HL, B <: HList](collection: QueryPath, values: A, where: Option[B]) extends QueryExpression[B] with QueryModify[B]
//  case class QueryDelete[B <:(collection: QueryPath, where: QueryComparison) extends QueryExpression

}
