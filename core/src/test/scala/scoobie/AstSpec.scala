package scoobie

import org.specs2._
import _root_.shapeless._
import scoobie.ast._

/**
  * Created by jbarber on 5/19/16.
  */
object AstSpec extends Specification
                  with ParamTests
                  with ProjectionTests
                  with PathTests
                  with ComparisonTests
                  with JoinTests { def is = s2"""
  Ast Construction
    Query Path
      Cons $pathCons
      End  $pathEnd

    Query Values
      Parameter         $param
      Raw String Expression   $rawExpression
      Function          $queryFunctionTest
      Add               $queryAddTest
      Sub               $querySubTest
      Div               $queryDivTest
      Mul               $queryMulTest
      Null              $queryNullParamsTest

    Query Comparisons
      Literal               $queryLitTest
      Equals                $simpleEqual
      Greater Than          $simpleGreaterThan
      Greater Than Or Equal $simpleGreaterThanOrEqual
      Less Than             $simpleLessThan
      Less Than Or Equal    $simpleLessThanOrEqual
      And                   $simpleAnd
      Or                    $simpleOr
      In                    $simpleIn
      NOP                   $queryComparisonNop

    Query Projections
      All                 $queryProjectAllTest
      One                 $queryProjectOneTest

    Query Joins
      Inner                  $simpleInnerJoin
      Left Outer             $simpleLeftOuterJoin
      Right Outer            $simpleRightOuterJoin
      Full Outer             $simpleFullOuterJoin
      Cross                  $simpleCrossJoin

"""
}
