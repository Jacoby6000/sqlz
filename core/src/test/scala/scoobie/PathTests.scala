package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */

trait PathTests extends SpecLike with TestHelpers {
  val columnPathEnd = PathEnd("column")
  val columnPathCons = PathCons("foo", PathEnd("bar"))

  lazy val pathEnd = {
    columnPathEnd.path mustEqual "column"
  }

  lazy val pathCons = {
    columnPathCons.path mustEqual "foo"
    columnPathCons.queryPath mustEqual PathEnd("bar")
  }
}
