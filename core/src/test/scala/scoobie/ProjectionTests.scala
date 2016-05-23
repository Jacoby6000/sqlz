package scoobie

import scoobie.ast._
import org.specs2._
import _root_.shapeless._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ProjectionTests extends SpecificationLike with ParamTests {
  lazy val queryProjectAllTest = QueryProjectAll.params mustEqual HNil
  lazy val queryProjectOneTest = {
    projection.alias mustEqual None
    projection.params mustEqual ("foo" :: HNil)
    projection.selection mustEqual fooParam
  }
}
