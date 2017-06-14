package scoobie.snacks.mild

import org.specs2._
import scoobie.ast._
import scoobie.coercion.Coerce
import scoobie.snacks.mild.sql._

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
    null (`null`)       ${`null` mustEqual QueryNull[DummyHKT]}
    Star (`*`)         ${`*` mustEqual QueryProjectAll[DummyHKT]}

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


  trait DummyHKT[A]

  implicit def coercer: Coerce[DummyHKT] = new Coerce[DummyHKT] {}
  implicit def dummyGen[A]: DummyHKT[A] = new DummyHKT[A] {}

  lazy val queryPathInterpolator = {
    p"foo" mustEqual QueryPathEnd("foo")
    p"foo.bar" mustEqual QueryPathCons("foo", QueryPathEnd("bar"))
  }

  lazy val rawExpressionInterprolator = {
    val blah = "baz"
    expr"foo $blah" mustEqual QueryRawExpression("foo baz")
    stringExpr.interpret(expr"foo $blah".t) mustEqual "foo baz"
  }

  lazy val queryFunctionInterpolator = func"foo"(p"bar", "baz", 5) mustEqual QueryFunction(QueryPathEnd("foo"), List(p"bar", "baz".asParam, 5.asParam))


  implicit class AExtensions[A](a: A) {
    def asParam: QueryValue[DummyHKT] = QueryParameter(a)
  }

  lazy val queryEquals = (QueryPathEnd("foo") === "bar") mustEqual QueryEqual[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryNotEquals1 = (QueryPathEnd("foo") !== "bar") mustEqual QueryNot[DummyHKT](QueryEqual(QueryPathEnd("foo"), QueryParameter("bar")))
  lazy val queryNotEquals2 = (QueryPathEnd("foo") <> "bar") mustEqual QueryNot(QueryEqual[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar")))
  lazy val queryLessThan = (QueryPathEnd("foo") < "bar") mustEqual QueryLessThan[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryLessThanOrEqual = (QueryPathEnd("foo") <= "bar") mustEqual QueryLessThanOrEqual[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryGreaterThan = (QueryPathEnd("foo") > "bar") mustEqual QueryGreaterThan[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryGreaterThanOrEqual = (QueryPathEnd("foo") >= "bar") mustEqual QueryGreaterThanOrEqual[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryAdd = (QueryPathEnd("foo") + "bar") mustEqual QueryAdd[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val querySub = (QueryPathEnd("foo") - "bar") mustEqual QuerySub[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryDiv = (QueryPathEnd("foo") / "bar") mustEqual QueryDiv[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryMul = (QueryPathEnd("foo") * "bar") mustEqual QueryMul[DummyHKT](QueryPathEnd("foo"), QueryParameter("bar"))
  lazy val queryAlias = (QueryPathEnd("foo") as "blah") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("blah"))
  lazy val queryIn1 = QueryPathEnd("foo") in ("a") mustEqual QueryIn[DummyHKT](QueryPathEnd("foo"), List("a".asParam))
  lazy val queryIn2 = QueryPathEnd("foo") in ("a", "b") mustEqual QueryIn[DummyHKT](QueryPathEnd("foo"), List("a".asParam, "b".asParam))

  val simpleEquals = p"foo" === "bar"

  lazy val and = (simpleEquals and simpleEquals) mustEqual QueryAnd(simpleEquals, simpleEquals)
  lazy val or = (simpleEquals or simpleEquals) mustEqual QueryOr(simpleEquals, simpleEquals)

  val simpleProjection = p"foo"
  val projectAll: QueryProjection[DummyHKT] = QueryProjectAll[DummyHKT]

  lazy val projectionAs = {
    (simpleProjection as "bar") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("bar"))
    (projectAll as "bar") mustEqual QueryProjectAll[DummyHKT]
  }
  lazy val on = (simpleProjection as "t" on simpleEquals) mustEqual ((simpleProjection as "t") -> simpleEquals)

  lazy val pathAs = {
    (QueryPathEnd("foo") as "bar") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("bar"))
    (QueryPathCons("baz", QueryPathEnd("foo")) as "bar") mustEqual QueryProjectOne(QueryPathCons("baz", QueryPathEnd("foo")), Some("bar"))
  }
  lazy val ascending = QueryPathEnd("foo").asc mustEqual QuerySortAsc(QueryPathEnd("foo"))
  lazy val descending = QueryPathEnd("foo").desc mustEqual QuerySortDesc(QueryPathEnd("foo"))

  val baseQuery = select(p"foo", p"bar") from p"baz"

  lazy val basicBuilder = baseQuery.build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List.empty, List.empty, None, None
    )
  )

  lazy val offset = (baseQuery offset 5).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List.empty, List.empty, Some(5), None
    )
  )

  lazy val limit = (baseQuery limit 5).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List.empty, List.empty, None, Some(5)
    )
  )

  lazy val offsetAndLimit = (baseQuery offset 5 limit 5).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List.empty, List.empty, Some(5), Some(5)
    )
  )

  lazy val orderBy = (baseQuery orderBy QueryPathEnd("foo").asc).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List(QuerySortAsc(QueryPathEnd[DummyHKT]("foo"))), List.empty, None, None
    )
  )

  lazy val groupBy = (baseQuery groupBy QueryPathEnd("foo").asc).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      List.empty, QueryComparisonNop[DummyHKT], List.empty, List(QuerySortAsc(QueryPathEnd[DummyHKT]("foo"))), None, None
    )
  )

  val join = QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None) on (QueryPathEnd("whatever") <> `null`)

  lazy val innerJoin = (baseQuery innerJoin join).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      (QueryInnerJoin(QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None), QueryPathEnd[DummyHKT]("whatever") <> `null`)) :: Nil,
      QueryComparisonNop[DummyHKT],
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val leftOuterJoin = (baseQuery leftOuterJoin join).build mustEqual (
    QuerySelect(
      QueryProjectOne[DummyHKT](QueryPathEnd("baz"), None),
      QueryProjectOne[DummyHKT](QueryPathEnd("foo"), None) ::
      QueryProjectOne[DummyHKT](QueryPathEnd("bar"), None) ::
      Nil,
      (QueryLeftOuterJoin(QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None), QueryPathEnd[DummyHKT]("whatever") <> `null`)) :: Nil,
      QueryComparisonNop[DummyHKT],
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val rightOuterJoin = (baseQuery rightOuterJoin join).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd[DummyHKT]("baz"), None),
      QueryProjectOne(QueryPathEnd[DummyHKT]("foo"), None) ::
      QueryProjectOne(QueryPathEnd[DummyHKT]("bar"), None) ::
      Nil,
      QueryRightOuterJoin(QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None), QueryPathEnd[DummyHKT]("whatever") <> `null`) :: Nil,
      QueryComparisonNop[DummyHKT],
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val fullOuterJoin = (baseQuery fullOuterJoin join).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd[DummyHKT]("baz"), None),
      QueryProjectOne(QueryPathEnd[DummyHKT]("foo"), None) ::
      QueryProjectOne(QueryPathEnd[DummyHKT]("bar"), None) ::
      Nil,
      QueryFullOuterJoin(QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None), QueryPathEnd[DummyHKT]("whatever") <> `null`) :: Nil,
      QueryComparisonNop[DummyHKT],
      List.empty,
      List.empty,
      None,
      None
    )
  )

  lazy val crossJoin = (baseQuery crossJoin join).build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd[DummyHKT]("baz"), None),
      QueryProjectOne(QueryPathEnd[DummyHKT]("foo"), None) ::
      QueryProjectOne(QueryPathEnd[DummyHKT]("bar"), None) ::
      Nil,
      QueryCrossJoin(QueryProjectOne(QueryPathEnd[DummyHKT]("inner"), None), QueryPathEnd[DummyHKT]("whatever") <> `null`) :: Nil,
      QueryComparisonNop[DummyHKT],
      List.empty,
      List.empty,
      None,
      None
    )
  )
}
