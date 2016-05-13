package com.github.jacoby6000.query
import doobie.imports._
import _root_.shapeless._
import _root_.shapeless.ops.hlist.{ FlatMapper, Mapper, Prepend, ToTraversable }
import com.github.jacoby6000.query.shapeless.KindConstraint.OfKindContainingHListTC._
import com.github.jacoby6000.query.shapeless.Polys._
import com.github.jacoby6000.query.shapeless.Typeclasses.{ HListUnwrapper, UnwrapAndFlattenHList, Unwrapper }
import HListUnwrapper._

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {

  implicit val queryProjectionUnwrapper = new Unwrapper[QueryProjection] {
    def unwrap[A <: HList](f: QueryProjection[A]): A = f.params
  }

  implicit val queryUnionUnwrapper = new Unwrapper[QueryUnion] {
    def unwrap[A <: HList](f: QueryUnion[A]): A = f.params
  }

  implicit val queryComparisonUnwrapper = new Unwrapper[QueryComparison] {
    def unwrap[A <: HList](f: QueryComparison[A]): A = f.params
  }

  implicit val modifyFieldUnwrapper = new Unwrapper[ModifyField] {
    def unwrap[A <: HList](f: ModifyField[A]): A = f.params
  }

  implicit val queryValueUnwrapper = new Unwrapper[QueryValue] {
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

  object QueryParameter {
    def apply[T](value: T)(implicit ev: T =:!= HList): QueryParameter[T :: HNil] = QueryParameter(value :: HNil)
  }

  object QueryFunction {
    def apply[L <: HList, Out <: HList](path: QueryPath, args: L)(implicit m: UnwrapAndFlattenHList.Aux[QueryValue, L, Out], toList: ToTraversable.Aux[L, List, QueryValue[_ <: HList]]): QueryFunction[Out] = QueryFunction[Out](path, toList(args), m(args))
  }

  object QueryAdd {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryAdd[Out] = QueryAdd(left, right, left.params ::: right.params)
  }

  object QuerySub {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QuerySub[Out] = QuerySub(left, right, left.params ::: right.params)
  }

  object QueryDiv {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryDiv[Out] = QueryDiv(left, right, left.params ::: right.params)
  }

  object QueryMul {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryMul[Out] = QueryMul(left, right, left.params ::: right.params)
  }

  sealed trait QueryValue[+L <: HList] { def params: L }
  case class QueryRawExpression[T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[HNil] { lazy val params: HNil = HNil }
  case class QueryParameter[T <: HList](value: T) extends QueryValue[T] { lazy val params: T = value }
  case class QueryFunction[L <: HList] private (path: QueryPath, args: List[QueryValue[_ <: HList]], params: L) extends QueryValue[L]
  case class QueryAdd[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
  case class QuerySub[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
  case class QueryDiv[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
  case class QueryMul[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
  case object QueryNull extends QueryValue[HNil] { lazy val params: HNil = HNil }

  object QueryEqual {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryEqual[Out] = QueryEqual(left, right, left.params ::: right.params)
  }

  object QueryNotEqual {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryNotEqual[Out] = QueryNotEqual(left, right, left.params ::: right.params)
  }

  object QueryGreaterThan {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryGreaterThan[Out] = QueryGreaterThan(left, right, left.params ::: right.params)
  }

  object QueryGreaterThanOrEqual {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryGreaterThanOrEqual[Out] = QueryGreaterThanOrEqual(left, right, left.params ::: right.params)
  }

  object QueryLessThan {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryLessThan[Out] = QueryLessThan(left, right, left.params ::: right.params)
  }

  object QueryLessThanOrEqual {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryValue[A], right: QueryValue[B])(implicit p: Prepend.Aux[A, B, Out]): QueryLessThanOrEqual[Out] = QueryLessThanOrEqual(left, right, left.params ::: right.params)
  }

  object QueryIn {
    def apply[A <: HList, B <: HList: OfKindContainingHList[QueryValue]#HL, MappedValues <: HList, Out <: HList](left: QueryValue[A], rights: B)(implicit toList: ToTraversable.Aux[B, List, QueryValue[_ <: HList]], m: Mapper.Aux[Unwrapper[QueryValue], B, MappedValues], p: Prepend.Aux[A, MappedValues, Out]): QueryIn[Out] = QueryIn[Out](left, toList(rights), left.params ::: m(rights))
  }

  object QueryAnd {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryAnd[Out] = QueryAnd(left, right, left.params ::: right.params)
  }

  object QueryOr {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryOr[Out] = QueryOr(left, right, left.params ::: right.params)
  }

  sealed trait QueryComparison[L <: HList] { def params: L }
  case class QueryEqual[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryNotEqual[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryGreaterThan[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryGreaterThanOrEqual[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryLessThan[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryLessThanOrEqual[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryIn[A <: HList] private (left: QueryValue[_ <: HList], rights: List[QueryValue[_ <: HList]], params: A) extends QueryComparison[A]
  case class QueryLit[A <: HList](value: QueryValue[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryNot[A <: HList](value: QueryComparison[A]) extends QueryComparison[A] { lazy val params: A = value.params }
  case class QueryAnd[A <: HList] private (left: QueryComparison[_ <: HList], right: QueryComparison[_ <: HList], params: A) extends QueryComparison[A]
  case class QueryOr[A <: HList] private (left: QueryComparison[_ <: HList], right: QueryComparison[_ <: HList], params: A) extends QueryComparison[A]

  sealed trait QueryPath extends QueryValue[HNil] { lazy val params: HNil = HNil }
  case class QueryPathEnd(path: String) extends QueryPath with QueryValue[HNil]
  case class QueryPathCons(path: String, queryPath: QueryPath) extends QueryPath with QueryValue[HNil]

  sealed trait QueryProjection[A <: HList] { def params: A }
  case class QueryProjectOne[A <: HList](selection: QueryValue[A], alias: Option[String]) extends QueryProjection[A] { lazy val params: A = selection.params }
  case object QueryProjectAll extends QueryProjection[HNil] { lazy val params: HNil = HNil }

  object QueryInnerJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryInnerJoin[Out] = QueryInnerJoin(table, on, table.params ::: on.params)
  }

  object QueryFullOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryFullOuterJoin[Out] = QueryFullOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryLeftOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryLeftOuterJoin[Out] = QueryLeftOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryRightOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryRightOuterJoin[Out] = QueryRightOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryCrossJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjectOne[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryCrossJoin[Out] = QueryCrossJoin(table, on, table.params ::: on.params)
  }

  sealed trait QueryUnion[A <: HList] { def params: A }
  case class QueryInnerJoin[A <: HList](table: QueryProjectOne[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryFullOuterJoin[A <: HList](table: QueryProjectOne[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryLeftOuterJoin[A <: HList](table: QueryProjectOne[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryRightOuterJoin[A <: HList](table: QueryProjectOne[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryCrossJoin[A <: HList](table: QueryProjectOne[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]

  sealed trait QuerySort
  case class QuerySortAsc(path: QueryPath) extends QuerySort
  case class QuerySortDesc(path: QueryPath) extends QuerySort

  sealed trait QueryExpression[A <: HList] { def params: A }
  sealed trait QueryModify[A <: HList] extends QueryExpression[A]

  object QuerySelect {
    def apply[Table <: HList, QueryProjections <: HList: OfKindContainingHList[QueryProjection]#HL, QueryUnions <: HList: OfKindContainingHList[QueryUnion]#HL, ComparisonParameters <: HList, MappedProjections <: HList, MappedUnions <: HList, Out1 <: HList, Out2 <: HList, Params <: HList](
      table: QueryProjection[Table],
      values: QueryProjections,
      unions: QueryUnions,
      filter: QueryComparison[ComparisonParameters],
      sorts: List[QuerySort],
      groupings: List[QuerySort],
      offset: Option[Int],
      limit: Option[Int]
    )(implicit
      mv: UnwrapAndFlattenHList.Aux[QueryProjection, QueryProjections, MappedProjections],
      mu: UnwrapAndFlattenHList.Aux[QueryUnion, QueryUnions, MappedUnions],
      p1: Prepend.Aux[Table, MappedProjections, Out1],
      p2: Prepend.Aux[Out1, MappedUnions, Out2],
      p3: Prepend.Aux[Out2, ComparisonParameters, Params],
      pl: ToTraversable.Aux[QueryProjections, List, QueryProjection[_ <: HList]],
      ul: ToTraversable.Aux[QueryUnions, List, QueryUnion[_ <: HList]]): QuerySelect[Params] =
      QuerySelect[Params](table, pl(values), ul(unions), Some(filter), sorts, groupings, offset, limit, p3(p2(p1(table.params, mv(values)), mu(unions)), filter.params))

    def apply[Table <: HList, QueryProjections <: HList: OfKindContainingHList[QueryProjection]#HL, QueryUnions <: HList: OfKindContainingHList[QueryUnion]#HL, MappedProjections <: HList, MappedUnions <: HList, Out1 <: HList, Params <: HList](
      table: QueryProjection[Table],
      values: QueryProjections,
      unions: QueryUnions,
      sorts: List[QuerySort],
      groupings: List[QuerySort],
      offset: Option[Int],
      limit: Option[Int]
    )(implicit
      mv: UnwrapAndFlattenHList.Aux[QueryProjection, QueryProjections, MappedProjections],
      mu: UnwrapAndFlattenHList.Aux[QueryUnion, QueryUnions, MappedUnions],
      p1: Prepend.Aux[Table, MappedProjections, Out1],
      p2: Prepend.Aux[Out1, MappedUnions, Params],
      pl: ToTraversable.Aux[QueryProjections, List, QueryProjection[_ <: HList]],
      ul: ToTraversable.Aux[QueryUnions, List, QueryUnion[_ <: HList]]): QuerySelect[Params] =
      QuerySelect[Params](table, pl(values), ul(unions), None, sorts, groupings, offset, limit, p2(p1(table.params, mv(values)), mu(unions)))

  }

  case class QuerySelect[Params <: HList] private (
    table: QueryProjection[_ <: HList],
    values: List[QueryProjection[_ <: HList]],
    unions: List[QueryUnion[_ <: HList]],
    filter: Option[QueryComparison[_ <: HList]],
    sorts: List[QuerySort],
    groupings: List[QuerySort],
    offset: Option[Int],
    limit: Option[Int],
    params: Params
  ) extends QueryExpression[Params] with QueryValue[Params]

  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A]) { lazy val params: A = value.params }
  //  case class QueryInsert[A <: HList : OfKindContainingHList[ModifyField]#HL, Out <: HList]
  //                        (collection: QueryPath, values: A)
  //                        (implicit m: Mapper.Aux[ModifyFieldUnwrapper.type, A, Out]) extends QueryExpression[Out] with QueryModify[Out] {
  //    lazy val params: Out = m(values)
  //  }

  //  case class QueryUpdate[A <: HList : OfKind[ModifyField]#HL, B <: HList](collection: QueryPath, values: A, where: Option[B]) extends QueryExpression[B] with QueryModify[B]
  //  case class QueryDelete[B <:(collection: QueryPath, where: QueryComparison) extends QueryExpression

}
