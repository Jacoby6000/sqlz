package scoobie

import org.specs2._
import _root_.shapeless._
import scoobie.ast._

/**
  * Created by jbarber on 5/19/16.
  */
object AstSpec extends Specification {
  def is =
s2"""
  Ast Construction
    Query Values
      Query Parameter         $param
      Query Path End          $pathEnd
      Raw String Expression   $rawExpression
      Query Function          $queryFunctionTest
      Query Add               $queryAddTest
      Query Sub               $querySubTest
      Query Div               $queryDivTest
      Query Mul               $queryMulTest
      Query Null              $queryNullParamsTest

    QueryComparisons
      Query Equals            $simpleEqual
      Query And               $simpleAnd

"""

  implicit val stringExpr = RawExpressionHandler[String](identity)

  implicit class AExtensions[A](val a: A) extends AnyVal {
    def asParam: QueryValue[A :: HNil] = QueryParameter(a)
  }

  val fooParam = QueryParameter("foo")
  val columnPathEnd = QueryPathEnd("column")
  val rawStringExpression = QueryRawExpression("some expr")
  val queryFunction = QueryFunction(columnPathEnd, "a".asParam :: "b".asParam :: 5.asParam :: HNil)
  val queryAdd = QueryAdd(fooParam, columnPathEnd)
  val querySub = QuerySub(fooParam, columnPathEnd)
  val queryDiv = QueryDiv(fooParam, columnPathEnd)
  val queryMul = QueryMul(fooParam, columnPathEnd)

  lazy val param = fooParam match {
    case QueryParameter(value) => value mustEqual ("foo" :: HNil)
  }

  lazy val pathEnd = columnPathEnd match {
    case QueryPathEnd(p) => p mustEqual "column"
  }

  lazy val rawExpression = rawStringExpression match {
    case QueryRawExpression(expr) =>
      expr mustEqual "some expr"
      stringExpr.interpret(expr) mustEqual "some expr"
  }

  lazy val queryFunctionTest = queryFunction.compare(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam), "a" :: "b" :: 5 :: HNil)
  lazy val queryAddTest = queryAdd.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val querySubTest = querySub.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryDivTest = queryDiv.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryMulTest = queryMul.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryNullParamsTest = QueryNull.params mustEqual HNil

  val equal = QueryEqual(fooParam, columnPathEnd)
  val and = QueryAnd(equal, equal)

  lazy val simpleEqual = equal.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleAnd = and.compare(equal, equal, "foo" :: "foo" :: HNil)

  implicit val queryAddBinaryExtractor = new BinaryExtractor[QueryAdd, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryAdd[A]) = (f.left, f.right, f.params)
  }

  implicit val querySubBinaryExtractor = new BinaryExtractor[QuerySub, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QuerySub[A]) = (f.left, f.right, f.params)
  }

  implicit val queryDivBinaryExtractor = new BinaryExtractor[QueryDiv, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryDiv[A]) = (f.left, f.right, f.params)
  }

  implicit val queryMulBinaryExtractor = new BinaryExtractor[QueryMul, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryMul[A]) = (f.left, f.right, f.params)
  }

  implicit val queryFunctionBinaryExtractor = new BinaryExtractor2[QueryFunction, QueryPath, List[QueryValue[_ <: HList]]] {
    def extract[A <: HList](f: QueryFunction[A]) = (f.path, f.args, f.params)
  }

  implicit val queryEqualBinaryExtractor = new BinaryExtractor[QueryEqual, QueryValue[_ <: HList]] {
    def extract[A <: HList](f: QueryEqual[A]) = (f.left, f.right, f.params)
  }

  implicit val queryAndBinaryExtractor = new BinaryExtractor[QueryAnd, QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryAnd[A]) = (f.left, f.right, f.params)
  }

  implicit val queryOrBinaryExtractor = new BinaryExtractor[QueryOr, QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryOr[A]) = (f.left, f.right, f.params)
  }

  implicit class BinaryExtractorExtensions[F[_ <: HList], A <: HList, B,C](f: F[A])(implicit binaryExtractor: BinaryExtractor2[F,B,C]) {
    def extract = binaryExtractor.extract(f)
    def compare[AA <: B, AB <: C](left: AA, right: AB, params: A) = binaryExtractor.compare(f)(left, right, params)
  }

  trait BinaryExtractor[F[_ <: HList], C] extends BinaryExtractor2[F,C,C]

  trait BinaryExtractor2[F[_ <: HList], LeftT, RightT] {
    def extract[A <: HList](f: F[A]): (_ <: LeftT, _ <: RightT, A)

    def compare[A <: HList,AA <: LeftT,AB <: RightT](f: F[A])(left: AA, right: AB, params: A) = {
      val (actualLeft, actualRight, p) = extract(f)
      actualLeft  mustEqual left
      actualRight mustEqual right
      p           mustEqual params
    }
  }
}
