package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait TestHelpers extends SpecificationLike {
  trait DummyHKT[T] {
    def show(t: T): T = t
  }

  implicit def dummyHKTGen[T]: DummyHKT[T] = new DummyHKT[T] {}

  implicit class AExtensions[A](a: A) {
    def asParam: QueryValue[DummyHKT] = QueryParameter[DummyHKT, A](a)
  }
}
