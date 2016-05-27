package scoobie

import scoobie.ast._
import org.specs2._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait TestHelpers extends SpecificationLike {
  implicit class AExtensions[A](a: A) {
    def asParam: QueryValue[A :: HNil] = QueryParameter(a)
  }

  trait BinaryExtractor2[F[_ <: HList], LeftT, RightT] {
    def extract[A <: HList](f: F[A]): (_ <: LeftT, _ <: RightT, A)

    def compare[A <: HList, AA <: LeftT, AB <: RightT](f: F[A])(left: AA, right: AB, params: A) = {
      val (actualLeft, actualRight, p) = extract(f)
      actualLeft mustEqual left
      actualRight mustEqual right
      p mustEqual params
    }
  }

  trait BinaryExtractor[F[_ <: HList], C] extends BinaryExtractor2[F, C, C]
}
