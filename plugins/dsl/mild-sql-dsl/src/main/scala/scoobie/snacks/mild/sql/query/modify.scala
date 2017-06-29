package scoobie.snacks.mild.sql.query

import scoobie.ast._

/**  * Created by jacob.barber on 5/24/16.
  */
trait modify {
  case class DeleteBuilder(table: QueryPath) {
    def where[G[_], B](queryComparison: QueryComparison[G, B]) = QueryDelete(table, queryComparison)
  }

  case class UpdateBuilder[G[_], A, B](table: QueryPath, values: List[ModifyField[A]], where: QueryComparison[G, B]) {
    def set(otherValues: List[ModifyField[A]]): UpdateBuilder[G, A, B] = this.copy(values = values ::: otherValues)
    def set(otherValues: ModifyField[A]*): UpdateBuilder[G, A, B] = this.set(otherValues.toList)
    def where(queryComparison: QueryComparison[G, B]): UpdateBuilder[G, A, B] = this.copy(where = QueryAnd.apply(where, queryComparison))
    def build: QueryUpdate[G, A, B] = QueryUpdate(table, values, where)
  }

  case class ModifyFieldBuilder(queryPath: QueryPath) {
    def ==>[F[_], A](value: QueryValue[F, A]): ModifyField[A] = ModifyField[A](queryPath, value)
  }

  case class InsertBuilder(table: QueryPath) {
    def values[A](values: ModifyField[A]*): QueryInsert[A] = QueryInsert(table, values.toList)
  }

}
