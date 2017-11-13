package scoobie

import scoobie.ast.ansi._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */

trait PathTests extends SpecLike with TestHelpers {
  val columnPathEnd = QueryPathEnd[DummyHKT]("column")
  val columnPathCons = QueryPathCons[DummyHKT]("foo", QueryPathEnd("bar"))

  lazy val pathEnd = {
    columnPathEnd.path mustEqual "column"
  }

  lazy val pathCons = {
    columnPathCons.path mustEqual "foo"
    columnPathCons.queryPath mustEqual QueryPathEnd[DummyHKT]("bar")
  }
}
