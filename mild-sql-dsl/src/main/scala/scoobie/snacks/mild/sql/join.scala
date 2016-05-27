package scoobie.snacks.mild.sql

import scoobie.ast._
import shapeless.HList
import shapeless.ops.hlist.Prepend

/**
  * Created by jacob.barber on 5/24/16.
  */
object join {
  trait Joiner[F[_ <: HList]] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out]
  }

  val leftJoiner = new Joiner[QueryLeftOuterJoin] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out] = QueryLeftOuterJoin(a, b)
  }

  val rightJoiner = new Joiner[QueryRightOuterJoin] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out] = QueryRightOuterJoin(a, b)
  }

  val outerJoiner = new Joiner[QueryFullOuterJoin] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out] = QueryFullOuterJoin(a, b)
  }

  val crossJoiner = new Joiner[QueryCrossJoin] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out] = QueryCrossJoin(a, b)
  }

  val innerJoiner = new Joiner[QueryInnerJoin] {
    def join[A <: HList, B <: HList, Out <: HList](a: QueryProjection[A], b: QueryComparison[B])(implicit prepender: Prepend.Aux[A, B, Out]): QueryUnion[Out] = QueryInnerJoin(a, b)
  }
}
