package scoobie

import scoobie.ast._
import org.specs2._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ComparisonTests extends SpecificationLike with TestHelpers with PathTests with ParamTests {
  implicit val queryInBinaryExtractor = new BinaryExtractor2[QueryIn, QueryValue[_ <: HList], List[QueryValue[_ <: HList]]] {
    def extract[A <: HList](f: QueryIn[A]) = (f.left, f.rights, f.params)
  }

  implicit val queryEqualBinaryExtractor = new BinaryExtractor[QueryEqual, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryEqual[A]) = (f.left, f.right, f.params)
  }

  implicit val queryGreaterThanBinaryExtractor = new BinaryExtractor[QueryGreaterThan, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryGreaterThan[A]) = (f.left, f.right, f.params)
  }

  implicit val queryGreaterThanOrEqualBinaryExtractor = new BinaryExtractor[QueryGreaterThanOrEqual, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryGreaterThanOrEqual[A]) = (f.left, f.right, f.params)
  }

  implicit val queryLessThanBinaryExtractor = new BinaryExtractor[QueryLessThan, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryLessThan[A]) = (f.left, f.right, f.params)
  }

  implicit val queryLessThanOrEqualBinaryExtractor = new BinaryExtractor[QueryLessThanOrEqual, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryLessThanOrEqual[A]) = (f.left, f.right, f.params)
  }

  implicit val queryAndBinaryExtractor = new BinaryExtractor[QueryAnd, QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryAnd[A]) = (f.left, f.right, f.params)
  }

  implicit val queryOrBinaryExtractor = new BinaryExtractor[QueryOr, QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryOr[A]) = (f.left, f.right, f.params)
  }

  val equal = QueryEqual(fooParam, columnPathEnd)
  val lessThan = QueryLessThan(fooParam, columnPathEnd)
  val lessThanOrEqual = QueryLessThanOrEqual(fooParam, columnPathEnd)
  val greaterThan = QueryGreaterThan(fooParam, columnPathEnd)
  val greaterThanOrEqual = QueryGreaterThanOrEqual(fooParam, columnPathEnd)
  val and = QueryAnd(equal, equal)
  val or = QueryOr(equal, equal)
  val queryLit = QueryLit(fooParam)
  val queryIn = QueryIn(columnPathEnd, "a".asParam :: "b".asParam :: 5.asParam :: HNil)

  lazy val queryComparisonNop = QueryComparisonNop.params mustEqual HNil
  lazy val simpleEqual = equal.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleLessThan = lessThan.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleLessThanOrEqual = lessThanOrEqual.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleGreaterThan = greaterThan.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleGreaterThanOrEqual = greaterThanOrEqual.compare(fooParam, columnPathEnd, "foo" :: HNil)

  lazy val simpleAnd = and.compare(equal, equal, "foo" :: "foo" :: HNil)
  lazy val simpleOr = or.compare(equal, equal, "foo" :: "foo" :: HNil)
  lazy val simpleIn = queryIn.compare(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam), "a" :: "b" :: 5 :: HNil)
  lazy val queryLitTest = {
    queryLit.value mustEqual fooParam
    queryLit.params mustEqual fooParam.params
  }
}
