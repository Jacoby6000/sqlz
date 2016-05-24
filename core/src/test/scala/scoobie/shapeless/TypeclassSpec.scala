package scoobie.shapeless

import org.specs2._
import scoobie.{JoinTests, ProjectionTests}
import scoobie.ast._
import scoobie.shapeless.Polys._
import scoobie.shapeless.Typeclasses.UnwrapAndFlattenHList
import _root_.shapeless._

/**
  * Created by jbarber on 5/23/16.
  */
object TypeclassSpec extends Specification with ProjectionTests with JoinTests {
  def is = s2"""
  Unwrappers
    Query Projection Unwrapper              $queryProjectionUnwrapper
    Query Value Unwrapper                   $queryValueUnwrapper
    Query Union Unwrapper                   $queryUnionUnwrapper
    Query Comparison Unwrapper              $queryComparisonUnwrapper
    Modify Field Unwrapper                  $modifyFieldUnwrapper
    HNil Unwrapper                          $hnilCaseUnwrapper
  """

  def unwrapAndFlattenQueryProjections[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryProjection, A, QueryProjectionUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryUnions[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryUnion, A, QueryUnionUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryComparisons[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryComparison, A, QueryComparisonUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryValues[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryValue, A, QueryValueUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenModifyFields[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[ModifyField, A, ModifyFieldUnwrapper.type, Out]) = unwrapAndFlattenHList(a)


  private val pjn: QueryProjection[String :: HNil] = projection
  private val prm: QueryValue[String :: HNil] = fooParam
  private val union: QueryUnion[String :: String :: HNil] = leftOuterJoin
  private val comparison: QueryComparison[String :: String :: HNil] = and
  private val modify: ModifyField[String :: HNil] = ModifyField(columnPathEnd, prm)

  lazy val queryProjectionUnwrapper =
    unwrapAndFlattenQueryProjections(pjn :: pjn :: HNil) mustEqual (projection.params.head :: projection.params.head :: HNil)

  lazy val queryValueUnwrapper =
    unwrapAndFlattenQueryValues(prm :: prm :: HNil) mustEqual (prm.params.head :: prm.params.head :: HNil)

  lazy val queryUnionUnwrapper =
    unwrapAndFlattenQueryUnions(union :: union :: HNil) mustEqual (union.params.head :: union.params.tail.head :: union.params.head :: union.params.tail.head :: HNil)

  lazy val queryComparisonUnwrapper =
    unwrapAndFlattenQueryComparisons(comparison :: comparison :: HNil) mustEqual (comparison.params.head :: comparison.params.tail.head :: comparison.params.head :: comparison.params.tail.head :: HNil)

  lazy val modifyFieldUnwrapper =
    unwrapAndFlattenModifyFields(modify :: modify :: HNil) mustEqual (modify.params.head :: modify.params.head :: HNil)

  lazy val hnilCaseUnwrapper =
    unwrapAndFlattenQueryProjections(HNil) mustEqual HNil
}
