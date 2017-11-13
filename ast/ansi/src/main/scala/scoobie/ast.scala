package scoobie.ast

/**
 * Created by jacob.barber on 2/2/16.
 */
object ansi {

  trait RawExpressionHandler[A] {
    def interpret(a: A): String
  }

  object RawExpressionHandler {
    def apply[A](f: A => String): RawExpressionHandler[A] = new RawExpressionHandler[A] {
      def interpret(a: A): String = f(a)
    }
  }

  sealed trait QueryValue[F[_]]
  case class QueryRawExpression[F[_], T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[F]
  case class QueryParameter[F[_], T](value: T)(implicit val ev: F[T]) extends QueryValue[F] {
    type Out = T
  }

  case class QueryFunction[F[_]] private (path: QueryPath[F], args: List[QueryValue[F]]) extends QueryValue[F]
  case class QueryAdd[F[_]] private (left: QueryValue[F], right: QueryValue[F]) extends QueryValue[F]
  case class QuerySub[F[_]] private (left: QueryValue[F], right: QueryValue[F]) extends QueryValue[F]
  case class QueryDiv[F[_]] private (left: QueryValue[F], right: QueryValue[F]) extends QueryValue[F]
  case class QueryMul[F[_]] private (left: QueryValue[F], right: QueryValue[F]) extends QueryValue[F]
  class QueryNull[F[_]] extends QueryValue[F] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryNull[_] => true
      case _ => false
    }

    override def toString: String = "QueryNull"
  }

  object QueryNull {
    def apply[F[_]]: QueryNull[F] = new QueryNull[F]
  }

  sealed trait QueryComparison[F[_]]
  class QueryComparisonNop[F[_]] extends QueryComparison[F] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryComparisonNop[_] => true
      case _ => false
    }

    override def toString: String = "QueryComparisonNop"
  }
  object QueryComparisonNop {
    def apply[F[_]]: QueryComparisonNop[F] = new QueryComparisonNop[F]
  }

  case class QueryEqual[F[_]](left: QueryValue[F], right: QueryValue[F]) extends QueryComparison[F]
  case class QueryGreaterThan[F[_]](left: QueryValue[F], right: QueryValue[F]) extends QueryComparison[F]
  case class QueryGreaterThanOrEqual[F[_]](left: QueryValue[F], right: QueryValue[F]) extends QueryComparison[F]
  case class QueryLessThan[F[_]](left: QueryValue[F], right: QueryValue[F]) extends QueryComparison[F]
  case class QueryLessThanOrEqual[F[_]](left: QueryValue[F], right: QueryValue[F]) extends QueryComparison[F]
  case class QueryAnd[F[_]](left: QueryComparison[F], right: QueryComparison[F]) extends QueryComparison[F]
  case class QueryOr[F[_]](left: QueryComparison[F], right: QueryComparison[F]) extends QueryComparison[F]
  case class QueryIn[F[_]](left: QueryValue[F], rights: List[QueryValue[F]]) extends QueryComparison[F]
  case class QueryLit[F[_]](value: QueryValue[F]) extends QueryComparison[F]
  case class QueryNot[F[_]](value: QueryComparison[F]) extends QueryComparison[F]

  sealed trait QueryPath[F[_]] extends QueryValue[F]
  case class QueryPathEnd[F[_]](path: String) extends QueryPath[F] with QueryValue[F]
  case class QueryPathCons[F[_]](path: String, queryPath: QueryPath[F]) extends QueryPath[F] with QueryValue[F]

  sealed trait QueryProjection[F[_]]
  case class QueryProjectOne[F[_]](selection: QueryValue[F], alias: Option[String]) extends QueryProjection[F]
  class QueryProjectAll[F[_]] extends QueryProjection[F] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryProjectAll[_] => true
      case _ => false
    }

    override def toString: String = "QueryProjectAll"
  }

  object QueryProjectAll {
    def apply[F[_]]: QueryProjectAll[F] = new QueryProjectAll[F]
  }


  sealed trait QueryJoin[F[_]]
  case class QueryInnerJoin[F[_]](table: QueryProjection[F], on: QueryComparison[F]) extends QueryJoin[F]
  case class QueryFullOuterJoin[F[_]](table: QueryProjection[F], on: QueryComparison[F]) extends QueryJoin[F]
  case class QueryLeftOuterJoin[F[_]](table: QueryProjection[F], on: QueryComparison[F]) extends QueryJoin[F]
  case class QueryRightOuterJoin[F[_]](table: QueryProjection[F], on: QueryComparison[F]) extends QueryJoin[F]
  case class QueryCrossJoin[F[_]](table: QueryProjection[F], on: QueryComparison[F]) extends QueryJoin[F]

  sealed trait QuerySort[F[_]]
  case class QuerySortAsc[F[_]](path: QueryPath[F]) extends QuerySort[F]
  case class QuerySortDesc[F[_]](path: QueryPath[F]) extends QuerySort[F]

  sealed trait QueryExpression[F[_]]
  sealed trait QueryModify[F[_]] extends QueryExpression[F]

  case class QuerySelect[F[_]](
                                table: QueryProjection[F],
                                values: List[QueryProjection[F]],
                                unions: List[QueryJoin[F]],
                                filter: QueryComparison[F],
                                sorts: List[QuerySort[F]],
                                groupings: List[QuerySort[F]],
                                offset: Option[Long],
                                limit: Option[Long]
  ) extends QueryExpression[F] with QueryValue[F]

  case class ModifyField[F[_]](key: QueryPath[F], value: QueryValue[F])
  case class QueryInsert[F[_]](collection: QueryPath[F], values: List[ModifyField[F]]) extends QueryExpression[F] with QueryModify[F]
  case class QueryUpdate[F[_]] private (collection: QueryPath[F], values: List[ModifyField[F]], where: QueryComparison[F]) extends QueryExpression[F] with QueryModify[F]
  case class QueryDelete[F[_]](collection: QueryPath[F], where: QueryComparison[F]) extends QueryExpression[F] with QueryModify[F]

}
