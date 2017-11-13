package scoobie

import scoobie.ast.ansi._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ComparisonTests extends SpecificationLike with TestHelpers with PathTests with ParamTests {

  val equal = QueryEqual(fooParam, columnPathEnd)
  val lessThan = QueryLessThan(fooParam, columnPathEnd)
  val lessThanOrEqual = QueryLessThanOrEqual(fooParam, columnPathEnd)
  val greaterThan = QueryGreaterThan(fooParam, columnPathEnd)
  val greaterThanOrEqual = QueryGreaterThanOrEqual(fooParam, columnPathEnd)
  val and = QueryAnd(equal, equal)
  val or = QueryOr(equal, equal)
  val queryLit = QueryLit(fooParam)
  val queryIn = QueryIn(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam))

  lazy val queryLitTest = {
    queryLit.value mustEqual fooParam
  }
}
