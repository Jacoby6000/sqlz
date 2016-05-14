val types = 'A' to 'Z'
val paramLists = types.scanLeft(List.empty[Char]){ case (a,b) => b :: a} map (_.reverse)
def makeProductApply(argKind: String, subTypeOf: List[String], contextBounds: List[String] ): List[String] = {

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
      val evidence = s"""ev: UnwrapAndFlattenHList.Aux[$argKind, ${typeList.map(s"$argKind[" + _ + "]").mkString("", " :: ", ":: HNil")}, ${argKind}Unwrapper.type, Out]"""

      val args = typeList.map(c => c.toLower + s": $argKind[" + c + "]")
      val argNames = typeList.map(c => c.toLower)
      val typesWithBounds = typeList.map(_ + typeConstraints)
      s"""def apply[${typesWithBounds.mkString(", ")}, Out <: HList](${args.mkString(", ")})(implicit $evidence) = applyProduct(${argNames.mkString("", " :: ", " :: HNil")})""".stripMargin
  }.toList
}
makeProductApply("QueryProjection", List("HList"), List.empty).mkString("\n")
