package com.github.jacoby6000.scoobie.shapeless

import shapeless.{ HList, Poly1 }

/**
 * Created by jacob.barber on 5/13/16.
 */
object Polys {
  trait UnwrapperPoly[F[_ <: HList]] extends Poly1 {
    type Out
    implicit def unwrapF[A <: HList]: Case.Aux[F[A], A] = at[F[A]](unwrap)
    def unwrap[A <: HList](f: F[A]): A
  }

  object UnwrapperPoly {
    type Aux[F[_ <: HList], Out0] = UnwrapperPoly[F] { type Out = Out0 }
    def apply[F[_ <: HList]](implicit unwrapper: UnwrapperPoly[F]): Aux[F, unwrapper.Out] = unwrapper
  }

  implicit def unwrapperPolyFinder[F[_ <: HList]](implicit unwrapperPoly: UnwrapperPoly[F]): UnwrapperPoly.Aux[F, unwrapperPoly.Out] = unwrapperPoly
}
