package scoobie

import _root_.shapeless._
import _root_.shapeless.ops.hlist.{Prepend, ToTraversable}
import scoobie.shapeless.Polys.UnwrapperPoly
import scoobie.shapeless.Typeclasses.{Combine4, UnwrapAndFlattenHList}

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {

  object QueryProjectionUnwrapper extends UnwrapperPoly[QueryProjection] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryProjection[A]): A = f.params
  }

  object QueryUnionUnwrapper extends UnwrapperPoly[QueryUnion] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryUnion[A]): A = f.params
  }

  object QueryComparisonUnwrapper extends UnwrapperPoly[QueryComparison] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryComparison[A]): A = f.params
  }

  object ModifyFieldUnwrapper extends UnwrapperPoly[ModifyField] {
    type Out = this.type
    def unwrap[A <: HList](f: ModifyField[A]): A = f.params
  }

  object QueryValueUnwrapper extends UnwrapperPoly[QueryValue] {
    type Out = this.type
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
    def apply[L <: HList, Out <: HList](path: QueryPath, args: L)(implicit m: UnwrapAndFlattenHList.Aux[QueryValue, L, QueryValueUnwrapper.type, Out], toList: ToTraversable.Aux[L, List, QueryValue[_ <: HList]]): QueryFunction[Out] = QueryFunction[Out](path, toList(args), m(args))
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
    def apply[A <: HList, B <: HList, MappedValues <: HList, Out <: HList](left: QueryValue[A], args: B)(implicit toList: ToTraversable.Aux[B, List, QueryValue[_ <: HList]], un: UnwrapAndFlattenHList.Aux[QueryValue, B, QueryValueUnwrapper.type, MappedValues], p: Prepend.Aux[A, MappedValues, Out]): QueryIn[Out] = QueryIn[Out](left, toList(args), left.params ::: un(args))
  }

  object QueryAnd {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryAnd[Out] = QueryAnd(left, right, left.params ::: right.params)
  }

  object QueryOr {
    def apply[A <: HList, B <: HList, Out <: HList](left: QueryComparison[A], right: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryOr[Out] = QueryOr(left, right, left.params ::: right.params)
  }

  sealed trait QueryComparison[L <: HList] { def params: L }
  case object QueryComparisonNop extends QueryComparison[HNil] { def params: HNil = HNil }
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
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjection[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryInnerJoin[Out] = QueryInnerJoin(table, on, table.params ::: on.params)
  }

  object QueryFullOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjection[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryFullOuterJoin[Out] = QueryFullOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryLeftOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjection[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryLeftOuterJoin[Out] = QueryLeftOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryRightOuterJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjection[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryRightOuterJoin[Out] = QueryRightOuterJoin(table, on, table.params ::: on.params)
  }

  object QueryCrossJoin {
    def apply[A <: HList, B <: HList, Out <: HList](table: QueryProjection[A], on: QueryComparison[B])(implicit p: Prepend.Aux[A, B, Out]): QueryCrossJoin[Out] = QueryCrossJoin(table, on, table.params ::: on.params)
  }

  sealed trait QueryUnion[A <: HList] { def params: A }
  case class QueryInnerJoin[A <: HList](table: QueryProjection[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryFullOuterJoin[A <: HList](table: QueryProjection[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryLeftOuterJoin[A <: HList](table: QueryProjection[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryRightOuterJoin[A <: HList](table: QueryProjection[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]
  case class QueryCrossJoin[A <: HList](table: QueryProjection[_ <: HList], on: QueryComparison[_ <: HList], params: A) extends QueryUnion[A]

  sealed trait QuerySort
  case class QuerySortAsc(path: QueryPath) extends QuerySort
  case class QuerySortDesc(path: QueryPath) extends QuerySort

  sealed trait QueryExpression[A <: HList] { def params: A }
  sealed trait QueryModify[A <: HList] extends QueryExpression[A]

  object QuerySelect {
    def apply[Table <: HList, QueryProjections <: HList, QueryUnions <: HList, ComparisonParameters <: HList, MappedProjections <: HList, MappedUnions <: HList, Params <: HList](
      table: QueryProjection[Table],
      values: QueryProjections,
      unions: QueryUnions,
      filter: QueryComparison[ComparisonParameters],
      sorts: List[QuerySort],
      groupings: List[QuerySort],
      offset: Option[Int],
      limit: Option[Int]
    )(implicit
      mv: UnwrapAndFlattenHList.Aux[QueryProjection, QueryProjections, QueryProjectionUnwrapper.type, MappedProjections],
      mu: UnwrapAndFlattenHList.Aux[QueryUnion, QueryUnions, QueryUnionUnwrapper.type, MappedUnions],
      p: Combine4.Aux[Table, MappedProjections, MappedUnions, ComparisonParameters, Params],
      pl: ToTraversable.Aux[QueryProjections, List, QueryProjection[_ <: HList]],
      ul: ToTraversable.Aux[QueryUnions, List, QueryUnion[_ <: HList]]): QuerySelect[Params] =
        QuerySelect[Params](table, pl(values), ul(unions), filter, sorts, groupings, offset, limit, p.combine(table.params, mv(values), mu(unions), filter.params))

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

  case class ModifyField[A <: HList](key: QueryPath, value: QueryValue[A]) { lazy val params: A = value.params }

  object QueryInsert {
    def apply[A <: HList, Out <: HList](collection: QueryPath, fields: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[ModifyField, A, ModifyFieldUnwrapper.type, Out], toList: ToTraversable.Aux[A, List, ModifyField[_ <: HList]]): QueryInsert[Out] = QueryInsert(collection, toList(fields), unwrapAndFlattenHList(fields))
  }

  case class QueryInsert[A <: HList] private (collection: QueryPath, values: List[ModifyField[_ <: HList]], params: A) extends QueryExpression[A] with QueryModify[A]

  object QueryUpdate {
    def apply[A <: HList, B <: HList, Out1 <: HList, Out2 <: HList](collection: QueryPath, values: A, where: QueryComparison[B])(implicit un: UnwrapAndFlattenHList.Aux[ModifyField, A, ModifyFieldUnwrapper.type, Out1], p1: Prepend.Aux[Out1, B, Out2], toList: ToTraversable.Aux[A, List, ModifyField[_ <: HList]]): QueryUpdate[Out2] = QueryUpdate(collection, toList(values), where, p1(un(values), where.params))
  }

  case class QueryUpdate[A <: HList] private (collection: QueryPath, values: List[ModifyField[_ <: HList]], where: QueryComparison[_ <: HList], params: A) extends QueryExpression[A] with QueryModify[A]

  case class QueryDelete[A <: HList](collection: QueryPath, where: QueryComparison[A]) extends QueryExpression[A] with QueryModify[A] { lazy val params = where.params }

}
