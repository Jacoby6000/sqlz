package scoobie

import _root_.shapeless._
import _root_.shapeless.contrib.scalacheck._
import org.specs2._
import scoobie.ast._
import org.scalacheck._
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll


/**
  * Created by jbarber on 5/19/16.
  */
object AstSpec extends Specification {
  def is =
    s2"""
      Ast Construction
        For Simple Query Equals            $simpleEqual
        For simple Query And               $simpleAnd

    """

//  sealed trait QueryValue[+L <: HList] { def params: L }
//  case class QueryRawExpression[T](t: T)(implicit val rawExpressionHandler: RawExpressionHandler[T]) extends QueryValue[HNil] { lazy val params: HNil = HNil }
//  case class QueryParameter[T <: HList](value: T) extends QueryValue[T] { lazy val params: T = value }
//  case class QueryFunction[L <: HList] private (path: QueryPath, args: List[QueryValue[_ <: HList]], params: L) extends QueryValue[L]
//  case class QueryAdd[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QuerySub[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QueryDiv[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case class QueryMul[A <: HList] private (left: QueryValue[_ <: HList], right: QueryValue[_ <: HList], params: A) extends QueryValue[A]
//  case object QueryNull extends QueryValue[HNil] { lazy val params: HNil = HNil }

  val fooParam = QueryParameter("foo")
  val columnPath = QueryPathEnd("column")

  val equal = QueryEqual(fooParam, columnPath)
  val and = QueryAnd(equal, equal)

  def validateEqual[T <: HList, A <: HList, B <: HList](queryEqual: QueryEqual[T], expectedLeft: QueryValue[A], expectedRight: QueryValue[A], expectedParams: T) = queryEqual match {
    case QueryEqual(left, right, params) =>
      left mustEqual expectedLeft
      right mustEqual expectedRight
      params mustEqual expectedParams
  }

  def validateAnd[T <: HList, A <: HList, B <: HList](queryAnd: QueryAnd[T], expectedLeft: QueryComparison[A], expectedRight: QueryComparison[A], expectedParams: T) = queryAnd match {
    case QueryAnd(left, right, params) =>
      left mustEqual expectedLeft
      right mustEqual expectedRight
      params mustEqual expectedParams
  }

  lazy val simpleEqual = validateEqual(equal, fooParam, columnPath, "foo" :: HNil)
  lazy val simpleAnd = validateAnd(and, equal, equal, "foo" :: "foo" :: HNil)




}
