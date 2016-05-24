package scoobie.shapeless

import scoobie.ast._
import shapeless.{HList, Poly1}

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

  object QueryProjectionUnwrapper extends UnwrapperPoly[QueryProjection] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryProjection[A]): A = f.params
  }

  object QueryUnionUnwrapper extends UnwrapperPoly[QueryUnion] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryUnion[A]): A = f.params
  }

  object QueryComparisonUnwrapper extends UnwrapperPoly[QueryComparison] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryComparison[A]): A = f.params
  }

  object ModifyFieldUnwrapper extends UnwrapperPoly[ModifyField] {
    type Out = this.type
    def unwrap[A <: HList](f: ModifyField[A]): A = f.params
  }

  object QueryValueUnwrapper extends UnwrapperPoly[QueryValue] {
    type Out = this.type
    def unwrap[A <: HList](f: QueryValue[A]): A = f.params
  }

}
