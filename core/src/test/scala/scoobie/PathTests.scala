package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */

trait PathTests extends SpecLike with TestHelpers {
  val columnPathEnd = QueryPathEnd("column")
  val columnPathCons = QueryPathCons("foo", QueryPathEnd("bar"))

  lazy val pathEnd = {
    columnPathEnd.path mustEqual "column"
  }

  lazy val pathCons = {
    columnPathCons.path mustEqual "foo"
    columnPathCons.queryPath mustEqual QueryPathEnd("bar")
  }
}
