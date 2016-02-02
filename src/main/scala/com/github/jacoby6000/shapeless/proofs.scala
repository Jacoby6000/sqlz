package com.github.jacoby6000.shapeless

import shapeless.ops.hlist
import shapeless.{HList, LUBConstraint, Nat, UnaryTCConstraint}

/**
  * Created by jacob.barber on 2/2/16.
  */
object proofs {
  type ContainsOnly[A <: HList, B] = LUBConstraint[A, B]
  type ContainsOnlyKind[A <: HList, F[_]] = UnaryTCConstraint[A, F]

  trait SameLengthAs[A <: HList, B <: HList] {
    type Out <: Nat
    implicit val aLength: hlist.Length.Aux[A, Out]
    implicit val bLength: hlist.Length.Aux[B, Out]
  }

  object SameLengthAs {
    implicit def apply[A <: HList, B <: HList, N <: Nat](implicit aLen: hlist.Length.Aux[A, N], bLen: hlist.Length.Aux[B,N]) = new SameLengthAs.Aux[A, B, N] {
      val aLength = aLen
      val bLength = bLen
    }

    trait Aux[A <: HList, B <: HList, Out0 <: Nat] extends SameLengthAs[A,B] { type Out = Out0 }
  }

}
