package scoobie

import org.specs2._
import scoobie.ast._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait JoinTests extends SpecificationLike with ParamTests with PathTests with ComparisonTests {

  implicit val innerJoinExtractor = new BinaryExtractor2[QueryInnerJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryInnerJoin[A]) = (f.table, f.on, f.params)
  }

  implicit val leftOuterJoinExtractor = new BinaryExtractor2[QueryLeftOuterJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryLeftOuterJoin[A]) = (f.table, f.on, f.params)
  }

  implicit val rightOuterJoinExtractor = new BinaryExtractor2[QueryRightOuterJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryRightOuterJoin[A]) = (f.table, f.on, f.params)
  }

  implicit val crossJoinExtractor = new BinaryExtractor2[QueryCrossJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryCrossJoin[A]) = (f.table, f.on, f.params)
  }

  implicit val fullOuterJoinExtractor = new BinaryExtractor2[QueryFullOuterJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryFullOuterJoin[A]) = (f.table, f.on, f.params)
  }


  val innerJoin = QueryInnerJoin(projection, equal)
  val leftOuterJoin = QueryLeftOuterJoin(projection, equal)
  val rightOuterJoin = QueryRightOuterJoin(projection, equal)
  val crossJoin = QueryCrossJoin(projection, equal)
  val fullOuterJoin = QueryFullOuterJoin(projection, equal)

  lazy val simpleInnerJoin = innerJoin.compare(projection, equal, "foo" :: "foo" :: HNil)
  lazy val simpleLeftOuterJoin = leftOuterJoin.compare(projection, equal, "foo" :: "foo" :: HNil)
  lazy val simpleRightOuterJoin = rightOuterJoin.compare(projection, equal, "foo" :: "foo" :: HNil)
  lazy val simpleCrossJoin = crossJoin.compare(projection, equal, "foo" :: "foo" :: HNil)
  lazy val simpleFullOuterJoin = fullOuterJoin.compare(projection, equal, "foo" :: "foo" :: HNil)
}
