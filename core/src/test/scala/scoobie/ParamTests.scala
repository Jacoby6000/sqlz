package scoobie

import scoobie.ast._
import org.specs2._

/**
  * Created by jacob.barber on 5/23/16.
  */
trait ParamTests extends SpecificationLike with PathTests with TestHelpers {

  val fooParam = Parameter[String, ANSIQuery[String]#fixed]("foo")
  val queryFunction = Function(columnPathEnd, List(HFix("a".asParam), HFix("b".asParam), HFix("c".asParam)))

  import ValueOperators._
  val queryAdd =
    ValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue[String, ANSIQuery[String]#fixed](columnPathEnd)), Add)

  val querySub =
    ValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue[String, ANSIQuery[String]#fixed](columnPathEnd)), Subtract)

  val queryDiv =
    ValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue[String, ANSIQuery[String]#fixed](columnPathEnd)), Divide)

  val queryMul =
    ValueBinOp[String, ANSIQuery[String]#fixed](HFix(fooParam), HFix(PathValue[String, ANSIQuery[String]#fixed](columnPathEnd)), Multiply)

  val projection = ProjectOne[String, ANSIQuery[String]#fixed](HFix(fooParam), None)

  lazy val param = {

    fooParam.value mustEqual "foo"
  }
}
