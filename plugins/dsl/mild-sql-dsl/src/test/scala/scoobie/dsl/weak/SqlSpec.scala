package scoobie.snacks.mild

import org.specs2._
import scoobie.ast._
import scoobie.coercion.Coerce
import scoobie.snacks.mild.sql._
import scoobie.cata._
import ComparisonValueOperators._
import ValueOperators._
import ComparisonOperators._
import JoinOperators._
import Indicies._
import SortType._

/**
  * Created by jacob.barber on 5/24/16.
  */

trait SqlDSLTestHelper {
  type AST[I] = QueryAST[String]#fixed[I]
  implicit val hfixQueryLifter = hfixLiftQuery[QueryAST[String]#of]
  val dsl = makeDSL(hfixQueryLifter)

  trait AsString[T] {
    def asString(t: T): String
  }

  implicit def asStringAny[A]: AsString[A] = new AsString[A] {
    def asString(a: A): String = a.toString
  }

  implicit val asStringQueryType: QueryType[AsString, String] = new QueryType[AsString, String] {
    def toQueryType[A](a: A, fa: AsString[A]): String = fa.asString(a)
  }

  implicit val coerce: Coerce[String, QueryAST[String]#fixed] = new Coerce[String, AST] {}


  implicit class TExtensions[T](t: T) {
    def asParam[F[_], U, A[_]](implicit qt: QueryType[F, U], fu: F[T], lifter: LiftQueryAST[U, A]): A[Value] = lifter.lift(Parameter[U, A](qt.toQueryType(t, fu)))
  }
}

class SqlSpec extends Specification with SqlDSLTestHelper {
  import dsl._
  def is = s2"""
  String Interpolators
    Query Path (p"...")  $queryPathInterpolator
    Query Function       $queryFunctionInterpolator

  Simple Aliases
    null (`null`)       ${`null` mustEqual Null.apply}
    Star (`*`)         ${`*` mustEqual ProjectAll.apply}

  Query Value Extensions
    Equals                     $queryEquals
    Not Equals !==             $queryNotEquals1
    Not Equals <>              $queryNotEquals2
    Less Than                  $queryLessThan
    Less Than Or Equal         $queryLessThanOrEqual
    Greater Than               $queryGreaterThan
    Greater Than Or Equal      $queryGreaterThanOrEqual
    Add                        $queryAdd
    Sub                        $querySub
    Div                        $queryDiv
    Mul                        $queryMul
    Alias                      $queryAlias
    In (1 param)               $queryIn1
    In (2 params)              $queryIn2
    Not In (2 params)          $queryNotIn
    Not                        $queryNot

  Query Path Extensions
    As           $pathAs
    Ascending    $ascending
    Descending   $descending

  Query Comparison Extensions
    And              $and
    Or               $or

  Query Projection Extensions
    As         $projectionAs
    On         $projectionOn
""" /*
  Query Builder
    Basic Builder                    $basicBuilder
    From Subquery                    $fromSubquery
    Select Scalar Subquery           $selectScalarSubquery
    Offset                           $offset
    Limit                            $limit
    Offset And Limit                 $offsetAndLimit
    Order By                         $orderBy
    Group By                         $groupBy
    Inner Join                       $innerJoin
    Full Outer Join                  $fullOuterJoin
    Left Outer Join                  $leftOuterJoin
    Right Outer Join                 $rightOuterJoin
    Cross Join                       $crossJoin
    Inner Join Builder               $innerJoinBuilder
    Full Outer Join Builder          $fullOuterJoinBuilder
    Left Outer Join Builder          $leftOuterJoinBuilder
    Right Outer Join Builder         $rightOuterJoinBuilder
    Cross Join Builder               $crossJoinBuilder
    Inner Join Subquery              $innerJoinSubquery
    Inner Join Builder Subquery      $innerJoinBuilderSubquery
    """ */

  lazy val queryPathInterpolator = {
    p"foo" mustEqual PathEnd("foo")
    p"foo.bar" mustEqual PathCons("foo", PathEnd("bar"))
  }

  lazy val queryFunctionInterpolator =
    func"foo"(p"bar", "baz".asParam, 5.asParam) mustEqual (
      Function[String, AST](PathEnd("foo"), List(HFix(PathValue[String, AST](p"bar")), "baz".asParam[AsString, String, AST], 5.asParam[AsString, String, AST]))
    )

  lazy val queryEquals =
    (p"foo" === "bar") mustEqual ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Equal)

  lazy val queryNotEquals1 =
    (p"foo" !== "bar") mustEqual Not[String, AST](HFix(ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Equal)))

  lazy val queryNotEquals2 =
    (p"foo" <> "bar") mustEqual Not[String, AST](HFix(ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Equal)))

  lazy val queryLessThan =
    (p"foo" < "bar") mustEqual ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), LessThan)

  lazy val queryLessThanOrEqual =
    (p"foo" <= "bar") mustEqual ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), LessThanOrEqual)

  lazy val queryGreaterThan =
    (p"foo" > "bar") mustEqual ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), GreaterThan)

  lazy val queryGreaterThanOrEqual =
    (p"foo" >= "bar") mustEqual ComparisonValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), GreaterThanOrEqual)

  lazy val queryAdd =
    (p"foo" + "bar") mustEqual ValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Add)

  lazy val querySub =
    (p"foo" - "bar") mustEqual ValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Subtract)

  lazy val queryDiv =
    (p"foo" / "bar") mustEqual ValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Divide)

  lazy val queryMul =
    (p"foo" * "bar") mustEqual ValueBinOp[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))), HFix(Parameter[String, AST]("bar")), Multiply)

  lazy val queryAlias =
    (p"foo" as "blah") mustEqual ProjectAlias[String, AST](HFix(ProjectOne[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))))), "blah")

  lazy val queryIn1 =
    p"foo" in ("a") mustEqual In[String, AST](PathEnd("foo"), List[AST[Value]]("a".asParam))

  lazy val queryIn2 =
    p"foo" in ("a", "b") mustEqual In[String, AST](PathEnd("foo"), List[AST[Value]]("a".asParam, "b".asParam))

  lazy val queryNotIn =
    p"foo" notIn ("a", "b") mustEqual Not[String, AST](HFix(In[String, AST](PathEnd("foo"), List[AST[Value]]("a".asParam, "b".asParam))))

  lazy val queryNot =
    dsl.not(p"foo") mustEqual Not[String, AST](HFix(Lit[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))))))

  val simpleEquals: QueryComparison[String, AST] = p"foo" === "bar"

  lazy val and = (simpleEquals and simpleEquals) mustEqual ComparisonBinOp(HFix(simpleEquals), HFix(simpleEquals), And)
  lazy val or = (simpleEquals or simpleEquals) mustEqual ComparisonBinOp(HFix(simpleEquals), HFix(simpleEquals), Or)

  val simpleProjection = ProjectOne[String, AST](p"foo")
  val projectAll: QueryProjection[String, AST] = `*`

  lazy val projectionAs = {
    (simpleProjection as "bar") mustEqual ProjectAlias[String, AST](HFix(ProjectOne[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))))), "bar")
    projectAll mustEqual ProjectAll[String, AST]
  }

  lazy val projectionOn = (simpleProjection as "t" on simpleEquals) mustEqual ((simpleProjection as "t") -> simpleEquals)

  lazy val pathAs = {
    (p"foo" as "bar") mustEqual ProjectAlias[String, AST](HFix(ProjectOne[String, AST](HFix(PathValue[String, AST](PathEnd("foo"))))), "bar")
    (p"baz.foo" as "bar") mustEqual ProjectAlias[String, AST](HFix(ProjectOne[String, AST](HFix(PathValue[String, AST](PathCons("baz", PathEnd("foo")))))), "bar")  }
  lazy val ascending = PathEnd("foo").asc mustEqual Sort(PathEnd("foo"), Ascending)
  lazy val descending = PathEnd("foo").desc mustEqual Sort(PathEnd("foo"), Descending)

  val manualTable = HFix(ProjectOne[String, AST](PathEnd("baz")))
  val manualColumns: List[AST[Projection]] = List(HFix(ProjectOne[String, AST](PathEnd("foo"))), HFix(ProjectOne[String, AST](PathEnd("bar"))))
  val manualBaseQuery = QuerySelect[String, AST](manualTable, manualColumns, List.empty, ComparisonNop[String, AST], List.empty, List.empty, None, None)

  val baseQuery = select(p"foo", p"bar") from p"baz"


  lazy val basicBuilder = baseQuery mustEqual manualBaseQuery

  val selectFromSub = select(p"foo", p"bar") from (select(p"foo", p"bar") from p"baz")

  lazy val fromSubquery = selectFromSub mustEqual (
    QuerySelect(manualBaseQuery, manualColumns, List.empty, ComparisonNop[String, AST], List.empty, List.empty, None, None)
  )

  val scalarSubquery = select(baseQuery) from p"baz"

  lazy val selectScalarSubquery = scalarSubquery mustEqual (
    QuerySelect[String, AST](
      manualTable,
      (HFix(ProjectOne[String, AST](HFix(manualBaseQuery))): AST[Projection]) :: Nil,
      List.empty, ComparisonNop[String, AST], List.empty, List.empty, None, None
    )
  )
  lazy val offsetTest = (baseQuery offset 5L) mustEqual (
    manualBaseQuery.copy(offset = Some(5))
  )

  lazy val limitTest = (baseQuery limit 5) mustEqual manualBaseQuery.copy(limit = Some(5))

  lazy val offsetAndLimit = (baseQuery offset 5 limit 5) mustEqual manualBaseQuery.copy(limit = Some(5), offset = Some(5))

  lazy val orderBy = (baseQuery orderBy p"foo".asc) mustEqual manualBaseQuery.copy(sorts = List(Sort(PathEnd("foo"), Ascending)))

  lazy val groupBy = (baseQuery groupBy p"foo".asc) mustEqual manualBaseQuery.copy(groupings = List(Sort(PathEnd("foo"), Ascending)))


  val joinTable = p"inner"
  val joinCondition: QueryComparison[String, AST] = p"whatever" <> `null`
/*
  lazy val innerJoinBuilder = (baseQuery innerJoin joinTable on joinCondition) mustEqual (
    manualBaseQuery.copy(joins =
      List(
        HFix(Join(
          HFix(ProjectOne(
            PathEnd("inner")
          )),
          HFix(Not(
            HFix(QueryComparisonBinOp(
              HFix(PathValue(PathEnd("whatever"))),
              HFix(QueryNull[String, AST]),
              Equal
            ))
          )),
          Inner
        ))
      )
    )
  )

  lazy val leftOuterJoinBuilder = (baseQuery leftOuterJoin joinTable on joinCondition) mustEqual (
    manualBaseQuery.copy(joins =
      List(
        HFix(Join(
          HFix(ProjectOne(
            PathEnd("inner")
          )),
          HFix(Not(
            HFix(QueryComparisonBinOp(
              HFix(PathValue(PathEnd("whatever"))),
              HFix(QueryNull[String, AST]),
              Equal
            ))
          )),
          LeftOuter
        ))
      )
    )
  )


  lazy val rightOuterJoinBuilder = (baseQuery rightOuterJoin joinTable on joinCondition) mustEqual (
    manualBaseQuery.copy(joins =
      List(
        HFix(Join(
          HFix(ProjectOne(
            PathEnd("inner")
          )),
          HFix(Not(
            HFix(QueryComparisonBinOp(
              HFix(PathValue(PathEnd("whatever"))),
              HFix(QueryNull[String, AST]),
              Equal
            ))
          )),
          RightOuter
        ))
      )
    )
  )

  lazy val fullOuterJoinBuilder = (baseQuery fullOuterJoin joinTable on joinCondition) mustEqual (
    manualBaseQuery.copy(joins =
      List(
        HFix(Join(
          HFix(ProjectOne(
            PathEnd("inner")
          )),
          HFix(Not(
            HFix(QueryComparisonBinOp(
              HFix(PathValue(PathEnd("whatever"))),
              HFix(QueryNull[String, AST]),
              Equal
            ))
          )),
          FullOuter
        ))
      )
    )
  )

  lazy val crossJoinBuilder = (baseQuery crossJoin joinTable on joinCondition) mustEqual (
    manualBaseQuery.copy(joins =
      List(
        HFix(Join(
          HFix(ProjectOne(
            PathEnd("inner")
          )),
          HFix(Not(
            HFix(QueryComparisonBinOp(
              HFix(PathValue(PathEnd("whatever"))),
              HFix(QueryNull[String, AST]),
              Equal
            ))
          )),
          Cartesian
        ))
      )
    )
  )
}

  val join = ProjectOne(PathEnd("inner"), None) on (PathEnd("whatever") <> `null`)

  lazy val innerJoin = (baseQuery innerJoin join) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      (InnerJoin(ProjectOne(PathEnd("inner"), None), PathEnd("whatever") <> `null`)) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val leftOuterJoin = (baseQuery leftOuterJoin join) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      (LeftOuterJoin(ProjectOne(PathEnd("inner"), None), PathEnd("whatever") <> `null`)) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val rightOuterJoin = (baseQuery rightOuterJoin join) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      RightOuterJoin(ProjectOne(PathEnd("inner"), None), PathEnd("whatever") <> `null`) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val fullOuterJoin = (baseQuery fullOuterJoin join) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      FullOuterJoin(ProjectOne(PathEnd("inner"), None), PathEnd("whatever") <> `null`) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val crossJoin = (baseQuery crossJoin join) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      CrossJoin(ProjectOne(PathEnd("inner"), None), PathEnd("whatever") <> `null`) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val innerJoinSubquery = (baseQuery innerJoin ((baseQuery as "foo") on joinCondition)) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      (InnerJoin(ProjectOne(baseQuery, Some("foo")), PathEnd("whatever") <> `null`)) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val innerJoinBuilderSubquery = (baseQuery innerJoin baseQuery on joinCondition) mustEqual (
    Select(
      ProjectOne(PathEnd("baz"), None),
      ProjectOne(PathEnd("foo"), None) ::
      ProjectOne(PathEnd("bar"), None) ::
      Nil,
      (InnerJoin(ProjectOne(baseQuery, None), PathEnd("whatever") <> `null`)) :: Nil,
      ComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )
*/
}
