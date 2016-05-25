package scoobie.dsl.weak

import org.specs2._
import scoobie.ast._
import _root_.shapeless._
import sql.{query, _}

/**
  * Created by jacob.barber on 5/24/16.
  */
class SqlSpec extends Specification { def is =
  s2"""
  String Interpolators
    Query Path (p"...")  $queryPathInterpolator
    Query Function       $queryFunctionInterpolator
    Raw Expression       $rawExpressionInterprolator

  Simple Aliases
    null (`null`)       ${`null` mustEqual QueryNull}
    Star (`*`)         ${`*` mustEqual QueryProjectAll}

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
    In (3 params)              $queryIn3

  Query Path Extensions
    As           $pathAs
    Ascending    $ascending
    Descending   $descending

  Query Comparison Extensions
    And              $and
    Or               $or

  Query Projection Extensions
    As         $projectionAs
    On         $on

  Query Builder
    Basic Builder                    $basicBuilder
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
    """



  lazy val queryPathInterpolator = {
    p"foo" mustEqual QueryPathEnd("foo")
    p"foo.bar" mustEqual QueryPathCons("foo", QueryPathEnd("bar"))
  }

  lazy val rawExpressionInterprolator = {
    val blah = "baz"
    expr"foo $blah" mustEqual QueryRawExpression("foo baz")
    stringExpr.interpret(expr"foo $blah".t) mustEqual "foo baz"
  }

  lazy val queryFunctionInterpolator = func"foo"(p"bar", "baz", 5) mustEqual QueryFunction(QueryPathEnd("foo"), QueryPathEnd("bar") :: "baz".asParam :: 5.asParam :: HNil)


  implicit class AExtensions[A](a: A) {
    def asParam: QueryValue[A :: HNil] = QueryParameter(a)
  }

  lazy val queryEquals = (QueryPathEnd("foo") === "bar") mustEqual QueryEqual(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryNotEquals1 = (QueryPathEnd("foo") !== "bar") mustEqual QueryNot(QueryEqual(QueryPathEnd("foo"), QueryParameter("bar")))
  lazy val queryNotEquals2 = (QueryPathEnd("foo") <> "bar") mustEqual QueryNot(QueryEqual(QueryPathEnd("foo"), QueryParameter("bar")))
  lazy val queryLessThan = (QueryPathEnd("foo") < "bar") mustEqual QueryLessThan(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryLessThanOrEqual = (QueryPathEnd("foo") <= "bar") mustEqual QueryLessThanOrEqual(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryGreaterThan = (QueryPathEnd("foo") > "bar") mustEqual QueryGreaterThan(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryGreaterThanOrEqual = (QueryPathEnd("foo") >= "bar") mustEqual QueryGreaterThanOrEqual(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryAdd = (QueryPathEnd("foo") + "bar") mustEqual QueryAdd(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val querySub = (QueryPathEnd("foo") - "bar") mustEqual QuerySub(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryDiv = (QueryPathEnd("foo") / "bar") mustEqual QueryDiv(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryMul = (QueryPathEnd("foo") * "bar") mustEqual QueryMul(QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryAlias = (QueryPathEnd("foo") as "blah") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("blah"))
  lazy val queryIn1 = QueryPathEnd("foo") in ("a") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: HNil)
  lazy val queryIn2 = QueryPathEnd("foo") in ("a", "b") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: HNil)
  lazy val queryIn3 = QueryPathEnd("foo") in ("a", "b", "c") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: "c".asParam :: HNil)
  lazy val queryIn4 = QueryPathEnd("foo") in ("a", "b", "c", "d") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: "c".asParam :: "d".asParam :: HNil)

  val simpleEquals = QueryPathEnd("foo") === "bar"

  lazy val and = (simpleEquals and simpleEquals) mustEqual QueryAnd(simpleEquals, simpleEquals)
  lazy val or = (simpleEquals or simpleEquals) mustEqual QueryOr(simpleEquals, simpleEquals)

  val simpleProjection = QueryProjectOne(QueryPathEnd("foo"), None)

  lazy val projectionAs = {
    (simpleProjection as "bar") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("bar"))
    (QueryProjectAll as "bar") mustEqual QueryProjectAll
  }
  lazy val on = (simpleProjection on simpleEquals) mustEqual (simpleProjection -> simpleEquals)

  lazy val pathAs = {
    (QueryPathEnd("foo") as "bar") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("bar"))
    (QueryPathCons("baz", QueryPathEnd("foo")) as "bar") mustEqual QueryProjectOne(QueryPathCons("baz", QueryPathEnd("foo")), Some("bar"))
  }
  lazy val ascending = QueryPathEnd("foo").asc mustEqual QuerySortAsc(QueryPathEnd("foo"))
  lazy val descending = QueryPathEnd("foo").desc mustEqual QuerySortDesc(QueryPathEnd("foo"))

  val baseQuery = select(QueryPathEnd("foo"), QueryPathEnd("bar")) from (QueryPathEnd("baz"))

  lazy val basicBuilder = baseQuery.build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
      (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
      HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List.empty, None, None
    )
  )

  lazy val offset = (baseQuery offset 5).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List.empty, Some(5), None
    )
  )

  lazy val limit = (baseQuery limit 5).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List.empty, None, Some(5)
    )
  )

  lazy val offsetAndLimit = (baseQuery offset 5 limit 5).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List.empty, Some(5), Some(5)
    )
  )

  lazy val orderBy = (baseQuery orderBy QueryPathEnd("foo").asc).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      HNil: HNil, QueryComparisonNop, List(QuerySortAsc(QueryPathEnd("foo"))), List.empty, None, None
    )
  )

  lazy val groupBy = (baseQuery groupBy QueryPathEnd("foo").asc).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List(QuerySortAsc(QueryPathEnd("foo"))), None, None
    )
  )

  val join = QueryProjectOne(QueryPathEnd("inner"), None) on (QueryPathEnd("whatever") <> `null`)

  lazy val innerJoin = (baseQuery innerJoin (join)).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
      (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
      HNil,
      (QueryInnerJoin(QueryProjectOne(QueryPathEnd("inner"), None), QueryPathEnd("whatever") <> `null`): QueryUnion[HNil]) :: HNil,
      QueryComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val leftOuterJoin = (baseQuery leftOuterJoin (join)).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      (QueryLeftOuterJoin(QueryProjectOne(QueryPathEnd("inner"), None), QueryPathEnd("whatever") <> `null`): QueryUnion[HNil]) :: HNil,
      QueryComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
    )

  lazy val rightOuterJoin = (baseQuery rightOuterJoin (join)).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      (QueryRightOuterJoin(QueryProjectOne(QueryPathEnd("inner"), None), QueryPathEnd("whatever") <> `null`): QueryUnion[HNil]) :: HNil,
      QueryComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
    )

  lazy val fullOuterJoin = (baseQuery fullOuterJoin (join)).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      (QueryFullOuterJoin(QueryProjectOne(QueryPathEnd("inner"), None), QueryPathEnd("whatever") <> `null`): QueryUnion[HNil]) :: HNil,
      QueryComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
    )

  lazy val crossJoin = (baseQuery crossJoin (join)).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
        (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
        HNil,
      (QueryCrossJoin(QueryProjectOne(QueryPathEnd("inner"), None), QueryPathEnd("whatever") <> `null`): QueryUnion[HNil]) :: HNil,
      QueryComparisonNop,
      List.empty,
      List.empty,
      None,
      None
    )
    )
}
