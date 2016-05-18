import com.github.jacoby6000.query.ast.{ModifyField, ModifyFieldUnwrapper}
import com.github.jacoby6000.query.shapeless.Typeclasses.UnwrapAndFlattenHList
import shapeless.{::, HList, HNil}
import shapeless.ops.hlist.Prepend

import scalaz._
import Scalaz._

val types = 'A' to 'Z'
val paramLists = types.scanLeft(List.empty[Char]){ case (a,b) => b :: a} map (_.reverse)

case class ApplyArguments(idx: Int, typeList: List[Char])
case class ApplyImplicits(types: List[String], evidence: List[String])

implicit val applyImplSemigroup = Monoid.instance[ApplyImplicits]((a,b) => ApplyImplicits(a.types ::: b.types, a.evidence ::: b.evidence), ApplyImplicits(List.empty, List.empty))

def makeProductApply(argKind: String, subTypeOf: List[String], contextBounds: List[String], evidences: List[ApplyArguments => ApplyImplicits]): List[String] = {

  val subTypeConstraints = subTypeOf match {
    case Nil => ""
    case constraints => " <: " + constraints.mkString(" <: ")
  }

  val contextBoundConstraints = contextBounds match {
    case Nil => ""
    case constraints => " : " + constraints.mkString(" : ")
  }

  val typeConstraints = subTypeConstraints + contextBoundConstraints
  paramLists.map {
    case Nil => """def apply = applyProduct(HNil)"""
    case typeList =>
      val ApplyImplicits(additionalTypes, evidenceList) = evidences.zipWithIndex.map(tup => tup._1(ApplyArguments(tup._2, typeList))).suml

      val args = typeList.map(c => c.toLower + s": $argKind[" + c + "]")
      val argNames = typeList.map(c => c.toLower)
      val typesWithBounds = typeList.map(_ + typeConstraints)
      s"""def apply[${typesWithBounds.mkString(", ")}, ${additionalTypes.mkString(", ")}](${args.mkString(", ")})(implicit ${evidenceList.mkString(", ")}) = applyProduct(${argNames.mkString("", " :: ", " :: HNil")})""".stripMargin
  }.toList
}

makeProductApply("QueryProjection", List("HList"), List.empty, List(
  { case ApplyArguments(idx, typeList) =>
      ApplyImplicits(
        List(s"Out_$idx <: HList"),
        List(s"""ev$idx: UnwrapAndFlattenHList.Aux[QueryProjection, ${typeList.map(s"QueryProjection[" + _ + "]").mkString("", " :: ", ":: HNil")}, QueryProjectionUnwrapper.type, Out_$idx]""")
      )
  }
)).mkString("\n")

makeProductApply("QueryValue", List("HList"), List.empty, List(
  { case ApplyArguments(idx, typeList) =>
      ApplyImplicits(
        List(s"Out_0 <: HList"),
        List(s"""ev0: UnwrapAndFlattenHList.Aux[QueryValue, ${typeList.map(s"QueryValue[" + _ + "]").mkString("", " :: ", ":: HNil")}, QueryValueUnwrapper.type, Out_0]""")
      )
  }
)).mkString("\n")

makeProductApply("ModifyField", List("HList"), List.empty, List(
  { case ApplyArguments(idx, typeList) =>
      val appendTypes = typeList.map(s"ModifyField[" + _ + "]").mkString("", " :: ", ":: HNil")
      ApplyImplicits(
        List("Appended <: HList", "Unwrapped0 <: HList", "POut <: HList"),
        List(
          s"""p1: Prepend.Aux[Values, $appendTypes, Appended]""",
          s"""un: UnwrapAndFlattenHList.Aux[ModifyField, Appended, ModifyFieldUnwrapper.type, Unwrapped0]""",
          s"""p2: Prepend.Aux[Unwrapped0, ComparisonParams, POut]""")
      )
  }
)).mkString("\n")




