package com.github.jacoby6000.query
import shapeless._
import shapeless.ops.hlist.{Comapped, LeftFolder, Mapped, Mapper, Prepend}


/**
  * Created by jacob.barber on 2/2/16.
  */
object ast {

  type OfKindContainingHList[TC[_ <: HList]] = {
    type HL[L <: HList] = UnaryTCConstraint[L, TC] forSome { type TC[_] }
  }

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

  case class QuerySelect[
                          Table <: HList,
                          QueryProjections <: HList : OfKindContainingHList[QueryProjection]#HL,
                          QueryUnions <: HList : OfKindContainingHList[QueryUnion]#HL,
                          QueryComparisons <: HList : OfKindContainingHList[QueryComparison]#HL,
                          MappedValues <: HList,
                          MappedUnions <: HList,
                          MappedFilters <: HList,
                          Out1 <: HList,
                          Out2 <: HList,
                          Out3 <: HList
                        ](
                          table: QueryProjection[Table],
                          values: QueryProjections,
                          unions: QueryUnions,
                          filters: QueryComparisons,
                          sorts: List[QuerySort],
                          groupings: List[QuerySort],
                          offset: Option[Int],
                          limit: Option[Int]
                        )(implicit
                          cv: Mapper.Aux[QueryProjectionUnwrapper.type, QueryProjections, MappedValues],
                          cu: Mapper.Aux[QueryUnionUnwrapper.type, QueryUnions, MappedUnions],
                          cf: Mapper.Aux[QueryComparisonUnwrapper.type, QueryComparisons, MappedFilters],
                          p1: Prepend.Aux[Table, MappedValues, Out1],
                          p2: Prepend.Aux[Out1, MappedUnions, Out2],
                          p3: Prepend.Aux[Out2, MappedFilters, Out3]
                        ) extends QueryExpression[Out3] with QueryValue[Out3] {
    lazy val params: Out3 = p3(p2(p1(table.params,cv(values)),cu(unions)), cf(filters))
  }

//  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A])
//  case class QueryInsert[A <: HList : OfKind[ModifyField]#HL](collection: QueryPath, values: A) extends QueryExpression[A] with QueryModify[A]
//  case class QueryUpdate[A <: HList : OfKind[ModifyField]#HL, B <: HList](collection: QueryPath, values: A, where: Option[B]) extends QueryExpression[B] with QueryModify[B]
//  case class QueryDelete[B <:(collection: QueryPath, where: QueryComparison) extends QueryExpression

//  QuerySelect(
//    table = QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil],
//    values = QueryProjectOne(QueryParameter("bar"), Some("b")) ::
//      QueryProjectOne(QueryParameter(5), Some("five")) ::
//      QueryProjectAll ::
//      HNil,
//    unions = QueryLeftOuterJoin(
//      table = QueryProjectOne(QueryPathEnd("table2"), Some("t2")),
//      on = QueryEqual(QueryPathEnd("t2"), QueryParameter(5))
//    ),
//    filters = QueryNot(QueryLit(QueryParameter(false))),
//    List.empty,
//    List.empty,
//    None,
//    None
//  )
}
