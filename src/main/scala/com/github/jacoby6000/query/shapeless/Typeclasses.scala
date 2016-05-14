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

  trait UnwrapAndFlattenHList[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F]] extends DepFn1[A] { type Out <: HList }

  object UnwrapAndFlattenHList {
    def apply[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F]](implicit unwrapAndFlattenHList: UnwrapAndFlattenHList[F, A, HF]): Aux[F, A, HF, unwrapAndFlattenHList.Out] = unwrapAndFlattenHList

    type Aux[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F], Out0] = UnwrapAndFlattenHList[F, A, HF] { type Out = Out0 }

    implicit def buildHNilUnflattener[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil, HF, HNil] = new UnwrapAndFlattenHList[F, HNil, HF] {
      type Out = HNil
      def apply(t: HNil): Out = HNil
    }

    implicit def buildUnflattener[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F], Out0 <: HList, Out1 <: HList](implicit m: HListUnwrapper.Aux[F, A, HF, Out0], fm: FlatMapper.Aux[Flatten.type, Out0, Out1]): Aux[F, A, HF, Out1] = new UnwrapAndFlattenHList[F, A, HF] {
      type Out = Out1
      def apply(t: A): Out1 = fm(m(t))
    }
  }

  trait UnwrapperPoly[F[_ <: HList]] extends Poly1 {
    type Out
    implicit def unwrapF[A <: HList]: Case[F[A]] = at[F[A]](unwrap)
    def unwrap[A <: HList](f: F[A]): A
  }

  object UnwrapperPoly {
    type Aux[F[_ <: HList], Out0] = UnwrapperPoly[F] { type Out = Out0 }
    def apply[F[_ <: HList]](implicit unwrapper: UnwrapperPoly[F]): Aux[F, unwrapper.Out] = unwrapper
  }

  implicit def unwrapperPolyFinder[F[_ <: HList]](implicit unwrapperPoly: UnwrapperPoly[F]): UnwrapperPoly.Aux[F, unwrapperPoly.Out] = unwrapperPoly

  trait HListUnwrapper[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F]] extends DepFn1[L] { type Out <: HList }

  object HListUnwrapper {
    type Aux[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F], Out0] = HListUnwrapper[F, L, HF] { type Out = Out0 }

    def apply[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F]](implicit hListUnwrapper: HListUnwrapper[F, L, HF]): Aux[F, L, HF, hListUnwrapper.Out] = hListUnwrapper

    implicit def buildHNilUnwrapper[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil, HF, HNil] = new HListUnwrapper[F, HNil, HF] {
      type Out = HNil
      def apply(t: HNil): HNil = HNil
    }

    implicit def buildUnwrapper[F[_ <: HList], L <: HList, HF <: UnwrapperPoly[F], Out0 <: HList](implicit mapper: Mapper.Aux[HF, L, Out0]): Aux[F, L, HF, Out0] = new HListUnwrapper[F, L, HF] {
      type Out = Out0
      def apply(t: L): Out0 = mapper(t)
    }
  }

}
