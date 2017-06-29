package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ParamTests extends SpecificationLike with PathTests with TestHelpers {

  val fooParam = QueryParameter[String, String]("foo")
  val queryFunction = QueryFunction(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam))

  import QueryValueArithmeticOperator._
  val queryAdd = QueryValueBinOp(fooParam, columnPathEnd, Add)
  val querySub = QueryValueBinOp(fooParam, columnPathEnd, Subtract)
  val queryDiv = QueryValueBinOp(fooParam, columnPathEnd, Divide)
  val queryMul = QueryValueBinOp(fooParam, columnPathEnd, Multiply)

  val projection = QueryProjectOne(fooParam, None)

  lazy val param = {
    fooParam.value mustEqual "foo"
  }
}
