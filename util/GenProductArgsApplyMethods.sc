import scalaz._, Scalaz._

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
