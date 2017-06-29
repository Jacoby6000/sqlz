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

  sealed trait QueryValue[A, B]
  case class QueryFunction[A, B](path: QueryPath, args: List[A]) extends QueryValue[A, B]
  case class QueryValueBinOp[A, B](left: A, right: A, op: QueryValueArithmeticOperator) extends QueryValue[A, B]
  case class QueryParameter[A, B](value: B) extends QueryValue[A, B]
  case class QueryPathValue[A, B](path: QueryPath) extends QueryValue[A, B]
  class QueryNull[A, B] extends QueryValue[A, B] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryNull[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryNull"
  }

  object QueryNull {
    def apply[A, B]: QueryNull[A, B] = new QueryNull[A, B]
  }

  sealed trait QueryComparisonValueOperator
  object QueryComparisonValueOperator {
    case object Equal extends QueryComparisonValueOperator
    case object GreaterThan extends QueryComparisonValueOperator
    case object GreaterThanOrEqual extends QueryComparisonValueOperator
    case object LessThan extends QueryComparisonValueOperator
    case object LessThanOrEqual extends QueryComparisonValueOperator
  }

  sealed trait QueryComparisonOperator
  object QueryComparisonOperator {
    case object And extends QueryComparisonOperator
    case object Or extends QueryComparisonOperator
  }

  sealed trait QueryComparison[A, B]
  case class QueryNot[A, B](value: A) extends QueryComparison[A, B]
  case class QueryComparisonBinOp[A, B](left: A, right: A, op: QueryComparisonOperator) extends QueryComparison[A, B]
  case class QueryComparisonValueBinOp[A, B](left: B, right: B, op: QueryComparisonValueOperator) extends QueryComparison[A, B]
  case class QueryIn[A, B](left: B, rights: List[B]) extends QueryComparison[A, B]
  case class QueryLit[A, B](value: B) extends QueryComparison[A, B]

  class QueryComparisonNop[A, B] extends QueryComparison[A, B] {
    override def equals(obj: scala.Any): Boolean = obj match {
      case _: QueryComparisonNop[_, _] => true
      case _ => false
    }

    override def toString: String = "QueryComparisonNop"
  }
  object QueryComparisonNop {
    def apply[A, B]: QueryComparisonNop[A, B] = new QueryComparisonNop[A, B]
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

  case class QueryJoin[A, B](table: QueryProjection[A], on: QueryComparison[B, A], joinOp: JoinOp)

  sealed trait SortType
  object SortType {
    case object Ascending extends SortType
    case object Descending extends SortType
  }
  case class QuerySort(column: QueryPath, sortType: SortType)

  case class QuerySelect[A, B, C](
                                table: QueryProjection[A],
                                values: List[QueryProjection[A]],
                                unions: List[QueryJoin[A, C]],
                                filter: QueryComparison[C, A],
                                sorts: List[QuerySort],
                                groupings: List[QuerySort],
                                offset: Option[Long],
                                limit: Option[Long]
  ) extends QueryValue[A, B]

  case class ModifyField[A](key: QueryPath, value: A)
  case class QueryInsert[A](collection: QueryPath, values: List[ModifyField[A]])
  case class QueryUpdate[A, B](collection: QueryPath, values: List[ModifyField[A]], where: QueryComparison[B, A])
  case class QueryDelete[A, B](collection: QueryPath, where: QueryComparison[A, B])

}
