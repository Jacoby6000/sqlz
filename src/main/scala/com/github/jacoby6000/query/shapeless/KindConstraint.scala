package com.github.jacoby6000.query.shapeless

import com.github.jacoby6000.query.ast.{ QueryProjection, QueryProjectionUnwrapper, QueryUnion }
import shapeless._
import shapeless.ops.hlist.{ Mapper, ToTraversable }

import scala.collection.mutable

/**
 * Created by jacob.barber on 5/10/16.
 */
object KindConstraint {

  trait ConstrainedUnaryTCConstraintTC[C] {
    trait ConstrainedUnaryTCConstraintTC[L <: HList, TC[_ <: C]] extends Serializable

    def apply[L <: HList, TC[_ <: C]](implicit utcc: ConstrainedUnaryTCConstraintTC[L, TC]): ConstrainedUnaryTCConstraintTC[L, TC] = utcc

    implicit def hnilUnaryTC1[TC[_ <: C]] = new ConstrainedUnaryTCConstraintTC[HNil, TC] {}
    implicit def hnilUnaryTC2[TC[_ <: C]] = new ConstrainedUnaryTCConstraintTC[HNil.type, TC] {}
    implicit def hlistUnaryTC1[H <: C, T <: HList, TC[_ <: C]](implicit utct: ConstrainedUnaryTCConstraintTC[T, TC]) =
      new ConstrainedUnaryTCConstraintTC[TC[H] :: T, TC] {}

    implicit def hlistUnaryTC2[L <: HList] = new ConstrainedUnaryTCConstraintTC[L, Id] {}

    implicit def hlistUnaryTC3[H] = new ConstrainedUnaryTCConstraintTC[HNil, Const[H]#λ] {}
    implicit def hlistUnaryTC4[H <: C, T <: HList](implicit utct: ConstrainedUnaryTCConstraintTC[T, Const[H]#λ]) =
      new ConstrainedUnaryTCConstraintTC[H :: T, Const[H]#λ] {}
  }

  object OfKindContainingHListTC extends ConstrainedUnaryTCConstraintTC[HList] {
    type OfKindContainingHList[TC[_ <: HList]] = {
      type HL[L <: HList] = ConstrainedUnaryTCConstraintTC[L, TC]
    }
  }

  import OfKindContainingHListTC._
}
