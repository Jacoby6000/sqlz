package scoobie

import org.specs2._
import scoobie.ast.{QueryComparison, QueryInnerJoin, QueryProjection}
import _root_.shapeless.HList

/**
  * Created by jacob.barber on 5/23/16.
  */
trait JoinTests extends SpecificationLike with ParamTests with PathTests with ComparisonTests with ComparisonTests {

  implicit val innerJoinExtractor = new BinaryExtractor2[QueryInnerJoin, QueryProjection[_ <: HList], QueryComparison[_ <: HList]] {
    def extract[A <: HList](f: QueryInnerJoin[A]) = (f.table, f.on, f.params)
  }

}
