package scoobie.snacks.mild.sql.query

import scoobie.ast._

/**  * Created by jacob.barber on 5/24/16.
  */
trait modify {
  case class DeleteBuilder[F[_]](table: QueryPath[F]) {
    def where(queryComparison: QueryComparison[F]) = QueryDelete(table, queryComparison)
  }

  case class UpdateBuilder[F[_]](table: QueryPath[F], values: List[ModifyField[F]], where: QueryComparison[F]) {
    def set(otherValues: List[ModifyField[F]]): UpdateBuilder[F] = this.copy(values = values ::: otherValues)
    def set(otherValues: ModifyField[F]*): UpdateBuilder[F] = this.set(otherValues.toList)
    def where(queryComparison: QueryComparison[F]): UpdateBuilder[F] = this.copy(where = QueryAnd.apply(where, queryComparison))
    def build: QueryUpdate[F] = QueryUpdate(table, values, where)
  }

  case class ModifyFieldBuilder[F[_]](queryPath: QueryPath[F]) {
    def ==>(value: QueryValue[F]): ModifyField[F] = ModifyField(queryPath, value)
  }

  case class InsertBuilder[F[_]](table: QueryPath[F]) {
    def values(values: ModifyField[F]*): QueryInsert[F] = QueryInsert(table, values.toList)
  }

}
