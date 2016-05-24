package scoobie

import org.specs2._
import _root_.shapeless._
import scoobie.ast._

/**
  * Created by jbarber on 5/19/16.
  */
object astspec extends Specification with ParamTests with ProjectionTests with PathTests with ComparisonTests {
  def is = s2"""
  Ast Construction
    Query Path
      Query Path Cons $pathCons
      Query Path End  $pathEnd

    Query Values
      Query Parameter         $param
      Raw String Expression   $rawExpression
      Query Function          $queryFunctionTest
      Query Add               $queryAddTest
      Query Sub               $querySubTest
      Query Div               $queryDivTest
      Query Mul               $queryMulTest
      Query Null              $queryNullParamsTest

    Query Comparisons
      Query Literal               $queryLitTest
      Query Equals                $simpleEqual
      Query Greater Than          $simpleGreaterThan
      Query Greater Than Or Equal $simpleGreaterThanOrEqual
      Query Less Than             $simpleLessThan
      Query Less Than Or Equal    $simpleLessThanOrEqual
      Query And                   $simpleAnd
      Query Or                    $simpleOr
      Query In                    $simpleIn
      Query NOP                   $queryComparisonNop

    Query Projections
      Project All                 $queryProjectAllTest
      Project One                 $queryProjectOneTest
"""
}
