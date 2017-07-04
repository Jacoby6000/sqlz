package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ComparisonTests extends SpecificationLike with TestHelpers with PathTests with ParamTests {

  import ComparisonValueOperators._
  val equal =
    ComparisonValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue(columnPathEnd)), Equal)

  val lessThan =
    ComparisonValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue(columnPathEnd)), LessThan)

  val lessThanOrEqual =
    ComparisonValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue(columnPathEnd)), LessThanOrEqual)

  val greaterThan =
    ComparisonValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue(columnPathEnd)), GreaterThan)

  val greaterThanOrEqual =
    ComparisonValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue(columnPathEnd)), GreaterThanOrEqual)

  import ComparisonOperators._
  val and = ComparisonBinOp[String, ANSIQuery[String]#fixed](HFix(equal), HFix(equal), And)
  val or = ComparisonBinOp[String, ANSIQuery[String]#fixed](HFix(equal), HFix(equal), Or)

  val queryLit =
    Lit[String, ANSIQuery[String]#fixed](HFix(fooParam))

  val queryIn =
    In[String, ANSIQuery[String]#fixed](HFix(PathValue(columnPathEnd)), List(HFix("a".asParam), HFix("b".asParam), HFix(5.asParam)))

  lazy val queryLitTest = {
    queryLit.value.unfix mustEqual fooParam
  }
}
