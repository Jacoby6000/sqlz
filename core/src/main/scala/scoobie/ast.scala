package scoobie

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {

  trait RawExpressionHandler[A] {
    def interpret(a: A): String
  }

  object RawExpressionHandler {
    def apply[A](f: A => String): RawExpressionHandler[A] = new RawExpressionHandler[A] {
      def interpret(a: A): String = f(a)
    }
  }

  sealed trait QueryValue[F[_], A]
  case class QueryParameter[F[_], A, T](value: T)(implicit val ev: F[T]) extends QueryValue[F, A]
  case class QueryFunction[F[_], A] private (path: QueryPath[F, A], args: List[A]) extends QueryValue[F, A]
  case class QueryAdd[F[_], A] private (left: A, right: A) extends QueryValue[F, A]
  case class QuerySub[F[_], A] private (left: A, right: A) extends QueryValue[F, A]
  case class QueryDiv[F[_], A] private (left: A, right: A) extends QueryValue[F, A]
  case class QueryMul[F[_], A] private (left: A, right: A) extends QueryValue[F, A]
  class QueryNull[F[_], A] extends QueryValue[F, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryNull[_] => true
      case _ => false
    }

    override def toString: String = "QueryNull"
  }

  object QueryNull {
    def apply[F[_], A]: QueryNull[F, A] = new QueryNull[F]
  }

  sealed trait QueryComparison[F[_], G[_], A]
  class QueryComparisonNop[F[_], G[_], A] extends QueryComparison[F, G, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryComparisonNop[_, _, _] => true
      case _ => false
    }

    override def toString: String = "QueryComparisonNop"
  }
  object QueryComparisonNop {
    def apply[F[_], G[_], A]: QueryComparisonNop[F, G, A] = new QueryComparisonNop[F, G, A]
  }

  case class QueryEqual[F[_], A, B](left: B, right: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryGreaterThan[F[_], A, B](left: B, right: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryGreaterThanOrEqual[F[_], A, B](left: B, right: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryLessThan[F[_], A, B](left: B, right: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryLessThanOrEqual[F[_], A, B](left: B, right: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryIn[F[_], A, B](left: B, rights: List[B])(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryLit[F[_], A, B](value: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryAnd[F[_], G[_], A](left: A, right: A) extends QueryComparison[F, A]
  case class QueryOr[F[_], G[_], A](left: A, right: A) extends QueryComparison[F, A]
  case class QueryNot[F[_], G[_], A](value: A) extends QueryComparison[F, A]

  sealed trait QueryPath[F[_], A] extends QueryValue[F, A]
  case class QueryPathEnd[F[_], A](path: String) extends QueryPath[F, A] with QueryValue[F, A]
  case class QueryPathCons[F[_], A](path: String, queryPath: QueryPath[F, A]) extends QueryPath[F, A] with QueryValue[F, A]

  sealed trait QueryProjection[A]
  case class QueryProjectOne[A](selection: A, alias: Option[String]) extends QueryProjection[A]
  class QueryProjectAll[A] extends QueryProjection[A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryProjectAll[_] => true
      case _ => false
    }

    override def toString: String = "QueryProjectAll"
  }

  object QueryProjectAll {
    def apply[A]: QueryProjectAll[A] = new QueryProjectAll[A]
  }


  sealed trait QueryJoin[A, B]
  case class QueryInnerJoin[A, B](table: A, on: B) extends QueryJoin[A, B]
  case class QueryFullOuterJoin[A, B](table: A, on: B) extends QueryJoin[A, B]
  case class QueryLeftOuterJoin[A, B](table: A, on: B) extends QueryJoin[A, B]
  case class QueryRightOuterJoin[A, B](table: A, on: B) extends QueryJoin[A, B]
  case class QueryCrossJoin[A, B](table: A, on: B) extends QueryJoin[A, B]

  sealed trait QuerySort[A]
  case class QuerySortAsc[A](path: A) extends QuerySort[A]
  case class QuerySortDesc[A](path: A) extends QuerySort[A]

  sealed trait QueryExpression[F[_], G[_]]
  sealed trait QueryModify[F[_], G[_]] extends QueryExpression[F, G]

  case class QuerySelect[F[_], G[_], A, B, C, D, E](
                                table: QueryProjection[A],
                                values: List[QueryProjection[A]],
                                unions: List[QueryJoin[A, B]],
                                filter: QueryComparison[G, C],
                                sorts: List[QuerySort[D]],
                                groupings: List[QuerySort[D]],
                                offset: Option[Long],
                                limit: Option[Long]
  ) extends QueryExpression[F, G] with QueryValue[F, E]

  case class ModifyField[K, V](key: K, value: V)
  case class QueryInsert[F[_], G[_], A, K, V](collection: QueryPath[F, A], values: List[ModifyField[K, V]]) extends QueryExpression[F, G] with QueryModify[F, G]
  case class QueryUpdate[F[_], G[_], A, B, K, V](collection: QueryPath[F, A], values: List[ModifyField[K, V]], where: QueryComparison[G, B]) extends QueryExpression[F, G] with QueryModify[F, G]
  case class QueryDelete[F[_], G[_], A, B](collection: QueryPath[F, A], where: QueryComparison[G, B]) extends QueryExpression[F, G] with QueryModify[F, G]

}
