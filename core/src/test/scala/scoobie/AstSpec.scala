package scoobie

import org.specs2._

/**
  * Created by jbarber on 5/19/16.
  */
object AstSpec extends Specification
                  with ParamTests
                  with ProjectionTests
                  with PathTests
                  with ComparisonTests { def is = s2"""
  Ast Construction
    Query Path
      Cons $pathCons
      End  $pathEnd

    Query Values
      Parameter         $param

    Query Comparisons
      Literal               $queryLitTest

    Query Projections
      One                 $queryProjectOneTest

    Query Joins
"""
}
