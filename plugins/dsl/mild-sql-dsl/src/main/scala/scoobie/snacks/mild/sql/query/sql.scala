package scoobie.snacks.mild

import scoobie.cata._

package object sql {
  def makeDSL[T, A[_]](liftAST: LiftQueryAST[T, A]): dsl[T, A] = new dsl[T, A] {
    val lifter = liftAST
  }
}
