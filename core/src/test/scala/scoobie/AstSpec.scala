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

    QueryComparisons
      Query Equals            $simpleEqual
      Query And               $simpleAnd

"""

//  sealed trait QueryValue[+L <: HList] { def params: L }
//  case class QueryAdd[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QuerySub[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QueryDiv[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QueryMul[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case object QueryNull extends QueryValue[HNil] { lazy val params: HNil = HNil }

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
    case QueryRawExpression(expr) => expr mustEqual "some expr"
  }

  lazy val queryFunctionTest =
    queryFunction match {
      case QueryFunction(path, args, p) =>
        args mustEqual List("a", "b", 5)
        path mustEqual columnPathEnd
        p    mustEqual ("a" :: "b" :: 5 :: HNil)
    }

  lazy val queryAddTest = queryAdd match {
    case QueryAdd(left,right,p) =>
      left  mustEqual fooParam
      right mustEqual columnPathEnd
      p     mustEqual ("foo" :: HNil)
  }

  lazy val querySubTest = querySub match {
    case QuerySub(left,right,p) =>
      left  mustEqual fooParam
      right mustEqual columnPathEnd
      p     mustEqual ("foo" :: HNil)
  }

  lazy val queryDivTest = queryDiv match {
    case QueryDiv(left,right,p) =>
      left  mustEqual fooParam
      right mustEqual columnPathEnd
      p     mustEqual ("foo" :: HNil)
  }

  lazy val queryMulTest = queryMul match {
    case QueryMul(left,right,p) =>
      left  mustEqual fooParam
      right mustEqual columnPathEnd
      p     mustEqual ("foo" :: HNil)
  }



  val equal = QueryEqual(fooParam, columnPathEnd)
  val and = QueryAnd(equal, equal)

  def validateEqual[T <: HList, A <: HList, B <: HList](queryEqual: QueryEqual[T], expectedLeft: QueryValue[A], expectedRight: QueryValue[A], expectedParams: T) = queryEqual match {
    case QueryEqual(left, right, params) =>
      left   mustEqual expectedLeft
      right  mustEqual expectedRight
      params mustEqual expectedParams
  }

  def validateAnd[T <: HList, A <: HList, B <: HList](queryAnd: QueryAnd[T], expectedLeft: QueryComparison[A], expectedRight: QueryComparison[A], expectedParams: T) = queryAnd match {
    case QueryAnd(left, right, params) =>
      left   mustEqual expectedLeft
      right  mustEqual expectedRight
      params mustEqual expectedParams
  }

  lazy val simpleEqual = validateEqual(equal, fooParam, columnPathEnd, "foo" :: HNil)
  lazy val simpleAnd = validateAnd(and, equal, equal, "foo" :: "foo" :: HNil)




}
