package scoobie

import scoobie.ast._
import org.specs2._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait PathTests extends SpecificationLike {
  val columnPathEnd = QueryPathEnd("column")
  val columnPathCons = QueryPathCons("foo", QueryPathEnd("bar"))

  lazy val pathEnd = {
    columnPathEnd.path mustEqual "column"
    columnPathEnd.params mustEqual HNil
  }

  lazy val pathCons = {
    columnPathCons.path mustEqual "foo"
    columnPathCons.queryPath mustEqual QueryPathEnd("bar")
    columnPathCons.params mustEqual HNil
  }
}
