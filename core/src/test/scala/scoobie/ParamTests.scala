package scoobie

import scoobie.ast._
import org.specs2._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ParamTests extends SpecificationLike with TestHelpers with PathTests {

  implicit class BinaryExtractorExtensions[F[_ <: HList], A <: HList, B, C](f: F[A])(implicit binaryExtractor: BinaryExtractor2[F, B, C]) {
    def extract = binaryExtractor.extract(f)
    def compare[AA <: B, AB <: C](left: AA, right: AB, params: A) = binaryExtractor.compare(f)(left, right, params)
  }

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

  implicit val stringExpr = RawExpressionHandler[String](identity)

  val fooParam = QueryParameter("foo")
  val rawStringExpression = QueryRawExpression("some expr")
  val queryFunction = QueryFunction(columnPathEnd, "a".asParam :: "b".asParam :: 5.asParam :: HNil)
  val queryAdd = QueryAdd(fooParam, columnPathEnd)
  val querySub = QuerySub(fooParam, columnPathEnd)
  val queryDiv = QueryDiv(fooParam, columnPathEnd)
  val queryMul = QueryMul(fooParam, columnPathEnd)

  val projection = QueryProjectOne(fooParam, None)

  lazy val param = {
    fooParam.value mustEqual ("foo" :: HNil)
    fooParam.params mustEqual fooParam.value
  }

  lazy val rawExpression = {
    rawStringExpression.t mustEqual "some expr"
    stringExpr.interpret(rawStringExpression.t) mustEqual "some expr"
    rawStringExpression.params mustEqual HNil
  }

  lazy val queryFunctionTest = queryFunction.compare(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam), "a" :: "b" :: 5 :: HNil)
  lazy val queryAddTest = queryAdd.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val querySubTest = querySub.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryDivTest = queryDiv.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryMulTest = queryMul.compare(fooParam, columnPathEnd, "foo" :: HNil)
  lazy val queryNullParamsTest = QueryNull.params mustEqual HNil
}
