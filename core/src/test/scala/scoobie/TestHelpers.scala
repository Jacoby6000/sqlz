package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait TestHelpers extends SpecificationLike {
  implicit class AExtensions[A](a: A) {
    def asParam = Parameter[String, ANSIQuery[String]#fixed](a.toString)
  }
}
