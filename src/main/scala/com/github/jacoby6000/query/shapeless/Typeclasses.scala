package com.github.jacoby6000.query.shapeless

import com.github.jacoby6000.query.shapeless.KindConstraint.OfKindContainingHListTC.OfKindContainingHList
import com.github.jacoby6000.query.shapeless.Polys._
import _root_.shapeless._
import _root_.shapeless.ops.hlist._
import _root_.shapeless.poly._

/**
 * Created by jacob.barber on 5/13/16.
 */
object Typeclasses {

  trait UnwrapAndFlattenHList[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F]] extends DepFn1[A] { type Out <: HList }

  object UnwrapAndFlattenHList {
    def apply[F[_ <: HList], A <: HList : OfKindContainingHList[F]#HL, HF <: UnwrapperPoly[F]](implicit unwrapAndFlattenHList: UnwrapAndFlattenHList[F, A, HF]): Aux[F, A, HF, unwrapAndFlattenHList.Out] = unwrapAndFlattenHList

    type Aux[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F], Out0] = UnwrapAndFlattenHList[F, A, HF] { type Out = Out0 }

    implicit def buildHNilUnflattener1[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil, HF, HNil] = new UnwrapAndFlattenHList[F, HNil, HF] {
      type Out = HNil
      def apply(t: HNil): Out = HNil
    }

    implicit def buildHNilUnflattener2[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil.type , HF, HNil.type ] = new UnwrapAndFlattenHList[F, HNil.type , HF] {
      type Out = HNil.type
      def apply(t: HNil.type ): Out = HNil
    }

    implicit def buildUnflattener[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F], Out0 <: HList, Out1 <: HList](implicit u: HListUnwrapper.Aux[F, A, HF, Out0], fm: FlatMapper.Aux[identity.type, Out0, Out1]): Aux[F, A, HF, Out1] = new UnwrapAndFlattenHList[F, A, HF] {
      type Out = Out1
      def apply(t: A): Out1 = fm(u(t))
    }
  }


  trait HListUnwrapper[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F]] extends DepFn1[L] { type Out <: HList }

  object HListUnwrapper {
    type Aux[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F], Out0] = HListUnwrapper[F, L, HF] { type Out = Out0 }

    def apply[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F]](implicit hListUnwrapper: HListUnwrapper[F, L, HF]): Aux[F, L, HF, hListUnwrapper.Out] = hListUnwrapper

    implicit def buildHNilUnwrapper1[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil, HF, HNil] = new HListUnwrapper[F, HNil, HF] {
      type Out = HNil
      def apply(t: HNil): HNil = HNil
    }

    implicit def buildHNilUnwrapper2[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil.type, HF, HNil.type] = new HListUnwrapper[F, HNil.type, HF] {
      type Out = HNil.type
      def apply(t: HNil.type): HNil.type = HNil
    }

    implicit def buildUnwrapper[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F], Out0 <: HList](implicit mapper: Mapper.Aux[HF, L, Out0]): Aux[F, L, HF, Out0] = new HListUnwrapper[F, L, HF] {
      type Out = Out0
      def apply(t: L): Out0 = mapper(t)
    }
  }

}
