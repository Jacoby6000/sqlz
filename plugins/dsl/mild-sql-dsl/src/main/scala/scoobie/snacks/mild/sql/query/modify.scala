package scoobie.snacks.mild.sql.query

import scoobie.ast._
import scoobie.cata._

/**  * Created by jacob.barber on 5/24/16.
  */
trait modify {
  case class DeleteBuilder[T, A[_]](table: Path) {
    def where(queryComparison: A[Indicies.Comparison]): QueryDelete[T, A] = QueryDelete(table, queryComparison)
  }

  class UpdateOps[T, A[_]](update: QueryUpdate[T, A]) {
    def set(otherValues: List[A[Indicies.ModifyFieldI]]): QueryUpdate[T, A] = update.copy(values = update.values ::: otherValues)
    def set(otherValues: A[Indicies.ModifyFieldI]*): QueryUpdate[T, A] = set(otherValues.toList)

    def where(queryComparison: A[Indicies.Comparison])(implicit lifter: LiftQueryAST[T, A]): QueryUpdate[T, A] =
      update.copy(where = lifter.lift(ComparisonBinOp(update.where, queryComparison, ComparisonOperators.And)))
  }

  case class ModifyFieldBuilder[T, A[_]](queryPath: Path) {
    def ==>(value: A[Indicies.Value]): ModifyField[T, A] = ModifyField[T, A](queryPath, value)
  }

  case class InsertBuilder[T, A[_]](table: Path) {
    def values(values: A[Indicies.ModifyFieldI]*): QueryInsert[T, A] = QueryInsert(table, values.toList)
  }

}
