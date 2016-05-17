package com.github.jacoby6000.query.shapeless

import com.github.jacoby6000.query.shapeless.Polys._
import _root_.shapeless._
import _root_.shapeless.ops.hlist._
import _root_.shapeless.poly._

/**
 * Created by jacob.barber on 5/13/16.
 */
object Typeclasses {

  type Combine2[A <: HList,B <: HList] = Prepend[A,B]
  object Combine2 {
    type Aux[A <: HList, B <: HList, Out <: HList] = Prepend.Aux[A,B,Out]
  }

  trait Combine3[A <: HList, B <: HList, C <: HList] {
    type Out <: HList
    def combine(a: A, b: B, c: C): Out
  }

  object Combine3 {
    def apply[A <: HList, B <: HList, C <: HList](implicit prepend2: Combine3[A,B,C]): Aux[A,B,C,prepend2.Out] = prepend2

    type Aux[A <: HList, B <: HList, C <: HList, Out0 <: HList] = Combine3[A,B,C] { type Out = Out0 }

    implicit def buildPrepender[A <: HList, B <: HList, C <: HList, Out0 <: HList, Out1 <: HList](implicit p1: Prepend.Aux[A,B,Out0], p2: Prepend.Aux[Out0, C, Out1]) = new Combine3[A,B,C] {
      type Out = Out1
      def combine(a: A, b: B, c: C): Out1 = p2(p1(a, b), c)
    }
  }

  trait Combine4[A <: HList, B <: HList, C <: HList, D <: HList] {
    type Out <: HList
    def combine(a: A, b: B, c: C, d: D): Out
  }

  object Combine4 {
    def apply[A <: HList, B <: HList, C <: HList, D <: HList](implicit prepend3: Combine4[A,B,C,D]): Aux[A,B,C,D,prepend3.Out] = prepend3

    type Aux[A <: HList, B <: HList, C <: HList, D <: HList, Out0 <: HList] = Combine4[A,B,C,D] { type Out = Out0 }

    implicit def buildPrepender[A <: HList, B <: HList, C <: HList, D <: HList, Out0 <: HList, Out1 <: HList, Out2 <: HList](implicit p1: Prepend.Aux[A,B,Out0], p2: Prepend.Aux[Out0, C, Out1], p3: Prepend.Aux[Out1, D, Out2]) = new Combine4[A,B,C,D] {
      type Out = Out2
      def combine(a: A, b: B, c: C, d: D): Out2 = p3(p2(p1(a, b), c), d)
    }

//    implicit def buildPrepender2[A <: HList, B <: HList, C <: HList, D <: HList, Out0 <: HList, Out1 <: HList](implicit p1: Prepend2.Aux[A,B,C,Out0], p2: Prepend.Aux[Out0, D, Out1]) = new Prepend3[A,B,C,D] {
//      type Out = Out1
//      def combine(a: A, b: B, c: C, d: D): Out1 = p2(p1.combine(a, b, c), d)
//    }
  }


  trait UnwrapAndFlattenHList[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F]] extends DepFn1[A] { type Out <: HList }

  object UnwrapAndFlattenHList {
    def apply[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F]](implicit unwrapAndFlattenHList: UnwrapAndFlattenHList[F, A, HF]): Aux[F, A, HF, unwrapAndFlattenHList.Out] = unwrapAndFlattenHList

    type Aux[F[_ <: HList], A <: HList, HF <: UnwrapperPoly[F], Out0] = UnwrapAndFlattenHList[F, A, HF] { type Out = Out0 }

    implicit def buildHNilUnflattener1[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil, HF, HNil] = new UnwrapAndFlattenHList[F, HNil, HF] {
      type Out = HNil
      def apply(t: HNil): Out = HNil
    }

    implicit def buildHNilUnflattener2[F[_ <: HList], HF <: UnwrapperPoly[F]]: Aux[F, HNil.type, HF, HNil.type] = new UnwrapAndFlattenHList[F, HNil.type, HF] {
      type Out = HNil.type
      def apply(t: HNil.type): Out = HNil
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
