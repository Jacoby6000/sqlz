package scoobie.shapeless

import org.specs2._
import scoobie.{JoinTests, ProjectionTests}
import scoobie.ast._
import scoobie.shapeless.Polys._
import scoobie.shapeless.Typeclasses._
import _root_.shapeless._

/**
  * Created by jbarber on 5/23/16.
  */
object TypeclassSpec extends Specification with ProjectionTests with JoinTests {
  def is = s2"""
  Unwrappers
    Query Projection              $queryProjectionUnwrapper
    Query Value                   $queryValueUnwrapper
    Query Union                   $queryUnionUnwrapper
    Query Comparison              $queryComparisonUnwrapper
    Modify Field                  $modifyFieldUnwrapper

  Prependers
    Combine3                           $combine3Test
    Combine4                           $combine4Test
  """

  def unwrapAndFlattenQueryProjections[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryProjection, A, QueryProjectionUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryUnions[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryUnion, A, QueryUnionUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryComparisons[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryComparison, A, QueryComparisonUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenQueryValues[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[QueryValue, A, QueryValueUnwrapper.type, Out]) = unwrapAndFlattenHList(a)
  def unwrapAndFlattenModifyFields[A <: HList, Out <: HList](a: A)(implicit unwrapAndFlattenHList: UnwrapAndFlattenHList.Aux[ModifyField, A, ModifyFieldUnwrapper.type, Out]) = unwrapAndFlattenHList(a)

  def unwrapQueryProjections[A <: HList, Out <: HList](a: A)(implicit unwrap: HListUnwrapper.Aux[QueryProjection, A, QueryProjectionUnwrapper.type, Out]) = unwrap(a)

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

  def combine3[A <: HList, B <: HList, C <: HList, Out <: HList](a: A, b: B, c: C)(implicit combine3: Combine3.Aux[A,B,C,Out]) =
    combine3.combine(a,b,c)

  def combine4[A <: HList, B <: HList, C <: HList, D <: HList, Out <: HList](a: A, b: B, c: C, d: D)(implicit combine4: Combine4.Aux[A,B,C,D,Out]) =
    combine4.combine(a,b,c,d)

  lazy val combine3Test = combine3("string" :: HNil, 5 :: HNil, 5d :: HNil) mustEqual ("string" :: 5 :: 5d :: HNil)
  lazy val combine4Test = combine4("string" :: HNil, 5 :: HNil, 5d :: HNil, "string" :: HNil) mustEqual ("string" :: 5 :: 5d :: "string" :: HNil)
}
