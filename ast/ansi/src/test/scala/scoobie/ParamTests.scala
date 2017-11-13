package scoobie

import scoobie.ast.ansi._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ParamTests extends SpecificationLike with PathTests with TestHelpers {

  implicit val stringExpr = RawExpressionHandler[String](identity)

  val fooParam = QueryParameter[DummyHKT, String]("foo")
  val rawStringExpression = QueryRawExpression("some expr")
  val queryFunction = QueryFunction(columnPathEnd, List("a".asParam, "b".asParam, 5.asParam))
  val queryAdd = QueryAdd(fooParam, columnPathEnd)
  val querySub = QuerySub(fooParam, columnPathEnd)
  val queryDiv = QueryDiv(fooParam, columnPathEnd)
  val queryMul = QueryMul(fooParam, columnPathEnd)

  val projection = QueryProjectOne(fooParam, None)

  lazy val param = {
    fooParam.value mustEqual "foo"
  }

  lazy val rawExpression = {
    rawStringExpression.t mustEqual "some expr"
    stringExpr.interpret(rawStringExpression.t) mustEqual "some expr"
  }
}
