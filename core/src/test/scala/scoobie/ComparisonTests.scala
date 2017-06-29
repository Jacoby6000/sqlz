package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ComparisonTests extends SpecificationLike with TestHelpers with PathTests with ParamTests {

  import QueryComparisonValueOperator._
  val equal = QueryComparisonValueBinOp(fooParam, columnPathEnd, Equal)
  val lessThan = QueryComparisonValueBinOp(fooParam, columnPathEnd, LessThan)
  val lessThanOrEqual = QueryComparisonValueBinOp(fooParam, columnPathEnd, LessThanOrEqual)
  val greaterThan = QueryComparisonValueBinOp(fooParam, columnPathEnd, GreaterThan)
  val greaterThanOrEqual = QueryComparisonValueBinOp(fooParam, columnPathEnd, GreaterThanOrEqual)

  import QueryComparisonOperator._
  val and = QueryComparisonBinOp(equal, equal, And)
  val or = QueryComparisonBinOp(equal, equal, Or)

  val queryLit = QueryLit(fooParam)
  val queryIn = QueryIn(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam))

  lazy val queryLitTest = {
    queryLit.value mustEqual fooParam
  }
}
