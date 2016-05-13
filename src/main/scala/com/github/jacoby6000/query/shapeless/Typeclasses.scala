package com.github.jacoby6000.query.shapeless

import _root_.shapeless._
import com.github.jacoby6000.query.shapeless.KindConstraint.OfKindContainingHListTC.OfKindContainingHList
import com.github.jacoby6000.query.shapeless.Polys._
import _root_.shapeless.ops.hlist._
import _root_.shapeless.poly._
import com.github.jacoby6000.query.ast.QueryUnion

/**
 * Created by jacob.barber on 5/13/16.
 */
object Typeclasses {
  trait UnwrapAndFlattenHList[F[_ <: HList], A <: HList] extends DepFn1[A] { type Out <: HList }

  object UnwrapAndFlattenHList {
    def apply[F[_ <: HList], A <: HList](implicit unwrapAndFlattenHList: UnwrapAndFlattenHList[F, A]): Aux[F, A, unwrapAndFlattenHList.Out] = unwrapAndFlattenHList

    type Aux[F[_ <: HList], A <: HList, Out0] = UnwrapAndFlattenHList[F, A] { type Out = Out0 }

    implicit def build[F[_ <: HList], A <: HList, Out0 <: HList, Out1 <: HList](implicit m: HListUnwrapper.Aux[F, A, Out0], fm: FlatMapper.Aux[Flatten.type, Out0, Out1]): Aux[F, A, Out1] = new UnwrapAndFlattenHList[F, A] {
      type Out = Out1

      override def apply(t: A): Out1 = fm(m(t))
    }
  }

  trait HListUnwrapper[F[_ <: HList], L <: HList] extends DepFn1[L] { type Out <: HList }

  object HListUnwrapper {
    type Aux[F[_ <: HList], L <: HList, Out0] = HListUnwrapper[F, L] { type Out = Out0 }

    def apply[F[_ <: HList], L <: HList](implicit hListUnwrapper: HListUnwrapper[F, L]): Aux[F, L, hListUnwrapper.Out] = hListUnwrapper

    implicit def build[F[_ <: HList], L <: HList, PolyOut <: Poly1, Out0 <: HList](implicit poly: UnwrapperPoly.Aux[F, PolyOut], mapper: Mapper.Aux[PolyOut, L, Out0]): Aux[F, L, Out0] = new HListUnwrapper[F, L] {
      override type Out = Out0

      override def apply(t: L): Out0 = mapper(t)
    }
  }

  trait UnwrapperPoly[F[_ <: HList]] extends Poly1 {
    type Out = this.type
    implicit def unwrap[A <: HList]: CaseBuilder[A]
  }

  object UnwrapperPoly {
    type Aux[F[_ <: HList], Out0] = UnwrapperPoly[F] { type Out = Out0 }

    def apply[F[_ <: HList]](implicit unwrapper: UnwrapperPoly[F]): Aux[F, unwrapper.Out] = unwrapper
  }

  implicit def unwrapperPolyDeriver[F[_ <: HList]](implicit F: Unwrapper[F]) = new Poly1 {
    implicit def unwrap[A <: HList] = at[F[A]](F.unwrap)
  }

  trait Unwrapper[F[_ <: HList]] {
    def unwrap[A <: HList](f: F[A]): A
  }

}
