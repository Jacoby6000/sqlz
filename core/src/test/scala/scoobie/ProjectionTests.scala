package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ProjectionTests extends SpecificationLike with ParamTests {
  lazy val queryProjectOneTest = {
    projection.alias mustEqual None
    projection.selection mustEqual fooParam
  }
}
