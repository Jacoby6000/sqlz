package scoobie

/**
 * Created by jacob.barber on 2/2/16.
 */
object ast {

  sealed trait QueryValueArithmeticOperator
  object QueryValueArithmeticOperator {
    case object Add extends QueryValueArithmeticOperator
    case object Subtract extends QueryValueArithmeticOperator
    case object Divide extends QueryValueArithmeticOperator
    case object Multiply extends QueryValueArithmeticOperator
  }

  sealed trait QueryValue[F[_], A]
  case class QueryParameter[F[_], A, T](value: T)(implicit val ev: F[T]) extends QueryValue[F, A]
  case class QueryFunction[F[_], A](path: QueryPath, args: List[A]) extends QueryValue[F, A]
  case class QueryValueBinOp[F[_], A](left: A, right: A, op: QueryValueArithmeticOperator) extends QueryValue[F, A]
  case class QueryPathValue[F[_], A](path: QueryPath) extends QueryValue[F, A]
  class QueryNull[F[_], A] extends QueryValue[F, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryNull[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryNull"
  }

  object QueryNull {
    def apply[F[_], A]: QueryNull[F, A] = new QueryNull[F, A]
  }

  sealed trait QueryValueComparisonOperator
  object QueryValueComparisonOperator {
    case object Equal extends QueryValueComparisonOperator
    case object GreaterThan extends QueryValueComparisonOperator
    case object GreaterThanOrEqual extends QueryValueComparisonOperator
    case object LessThan extends QueryValueComparisonOperator
    case object LessThanOrEqual extends QueryValueComparisonOperator
  }

  sealed trait QueryComparisonOperator
  object QueryComparisonOperator {
    case object And extends QueryComparisonOperator
    case object Or extends QueryValueComparisonOperator
  }

  sealed trait QueryComparison[F[_], A]
  case class QueryComparisonValueBinOp[F[_], A, B](left: B, right: B, op: QueryValueComparisonOperator)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryComparisonBinOp[F[_], A](left: A, right: A, op: QueryComparisonOperator) extends QueryComparison[F, A]
  case class QueryIn[F[_], A, B](left: B, rights: List[B])(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryLit[F[_], A, B](value: B)(implicit val ev: F[B]) extends QueryComparison[F, A]
  case class QueryNot[F[_], A](value: A) extends QueryComparison[F, A]

  class QueryComparisonNop[F[_], A] extends QueryComparison[F, A] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryComparisonNop[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryComparisonNop"
  }
  object QueryComparisonNop {
    def apply[F[_], A]: QueryComparisonNop[F, A] = new QueryComparisonNop[F, A]
  }


  sealed trait QueryPath
  case class QueryPathEnd(path: String) extends QueryPath
  case class QueryPathCons(path: String, queryPath: QueryPath) extends QueryPath

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

  sealed trait JoinOp
  object JoinOp {
    case object FullOuter extends JoinOp
    case object LeftOuter extends JoinOp
    case object RightOuter extends JoinOp
    case object Cartesian extends JoinOp
    case object Inner extends JoinOp
  }

  case class QueryJoin[F[_], A, B](table: QueryProjection[A], on: QueryComparison[F, B], joinOp: JoinOp)

  sealed trait SortType
  object SortType {
    case object Ascending extends SortType
    case object Descending extends SortType
  }
  case class QuerySort(column: QueryPath, sortType: SortType)

  case class QuerySelect[F[_], G[_], A, B](
                                table: QueryProjection[A],
                                values: List[QueryProjection[A]],
                                unions: List[QueryJoin[G, A, B]],
                                filter: QueryComparison[G, B],
                                sorts: List[QuerySort],
                                groupings: List[QuerySort],
                                offset: Option[Long],
                                limit: Option[Long]
  ) extends QueryValue[F, A]

  case class ModifyField[A](key: QueryPath, value: A)
  case class QueryInsert[A](collection: QueryPath, values: List[ModifyField[A]])
  case class QueryUpdate[F[_], A, B](collection: QueryPath, values: List[ModifyField[A]], where: QueryComparison[F, B])
  case class QueryDelete[F[_], A](collection: QueryPath, where: QueryComparison[F, A])

}
