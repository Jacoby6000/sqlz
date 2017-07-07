package scoobie.snacks.mild.sql.query

import scoobie.ast._
import scoobie.cata._

/**  * Created by jacob.barber on 5/24/16.
  */
trait modify[T, A[_]] {

  def lifter: LiftQueryAST[T, A]

  case class DeleteBuilder(table: Path) {
    def where(queryComparison: A[Indicies.Comparison]): QueryDelete[T, A] = QueryDelete(table, queryComparison)
  }

  class UpdateOps(update: QueryUpdate[T, A]) {
    def set(otherValues: List[A[Indicies.ModifyFieldI]]): QueryUpdate[T, A] = update.copy(values = update.values ::: otherValues)
    def set(otherValues: A[Indicies.ModifyFieldI]*): QueryUpdate[T, A] = set(otherValues.toList)

    def where(queryComparison: A[Indicies.Comparison]): QueryUpdate[T, A] =
      update.copy(where = lifter.lift(ComparisonBinOp(update.where, queryComparison, ComparisonOperators.And)))
  }

  case class ModifyFieldBuilder(queryPath: Path) {
    def ==>(value: A[Indicies.Value]): ModifyField[T, A] = ModifyField[T, A](queryPath, value)
  }

  case class InsertBuilder(table: Path) {
    def values(values: A[Indicies.ModifyFieldI]*): QueryInsert[T, A] = QueryInsert(table, values.toList)
  }

}
