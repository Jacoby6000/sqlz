package scoobie.dsl.weak

import org.specs2._
import scoobie.ast._
import _root_.shapeless._
import sql._

/**
  * Created by jacob.barber on 5/24/16.
  */
class SqlSpec extends Specification { def is =
  s2"""
  Query Value Extensions
    Query Equals                     $queryEquals
    Query Not Equals !==             $queryNotEquals1
    Query Not Equals <>              $queryNotEquals2
    Query Less Than                  $queryLessThan
    Query Less Than Or Equal         $queryLessThanOrEqual
    Query Greater Than               $queryGreaterThan
    Query Greater Than Or Equal      $queryGreaterThanOrEqual
    Query Add                        $queryAdd
    Query Sub                        $querySub
    Query Div                        $queryDiv
    Query Mul                        $queryMul
    Query Alias                      $queryAlias
    Query In (1 param)               $queryIn1
    Query In (2 params)              $queryIn2
    Query In (3 params)              $queryIn3

  Query Builder
    Basic Builder                    $basicBuilder
    """

  implicit class AExtensions[A](a: A) {
    def asParam: QueryValue[A :: HNil] = QueryParameter(a)
  }


  lazy val queryEquals = (p"foo" === "bar") mustEqual QueryEqual(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryNotEquals1 = (p"foo" !== "bar") mustEqual QueryNot(QueryEqual(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil))
  lazy val queryNotEquals2 = (p"foo" <> "bar") mustEqual QueryNot(QueryEqual(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil))
  lazy val queryLessThan = (p"foo" < "bar") mustEqual QueryLessThan(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryLessThanOrEqual = (p"foo" <= "bar") mustEqual QueryLessThanOrEqual(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryGreaterThan = (p"foo" > "bar") mustEqual QueryGreaterThan(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryGreaterThanOrEqual = (p"foo" >= "bar") mustEqual QueryGreaterThanOrEqual(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryAdd = (p"foo" + "bar") mustEqual QueryAdd(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val querySub = (p"foo" - "bar") mustEqual QuerySub(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryDiv = (p"foo" / "bar") mustEqual QueryDiv(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryMul = (p"foo" * "bar") mustEqual QueryMul(QueryPathEnd("foo"), QueryParameter("bar"), "bar" :: HNil)
  lazy val queryAlias = (p"foo" as "blah") mustEqual QueryProjectOne(QueryPathEnd("foo"), Some("blah"))
  lazy val queryIn1 = p"foo" in ("a") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: HNil)
  lazy val queryIn2 = p"foo" in ("a", "b") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: HNil)
  lazy val queryIn3 = p"foo" in ("a", "b", "c") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: "c".asParam :: HNil)
  lazy val queryIn4 = p"foo" in ("a", "b", "c", "d") mustEqual QueryIn(QueryPathEnd("foo"), "a".asParam :: "b".asParam :: "c".asParam :: "d".asParam :: HNil)

  val baseQuery = select(p"foo", p"bar") from (p"baz")

  lazy val basicBuilder = baseQuery.build mustEqual (
    QuerySelect(
      QueryProjectOne(QueryPathEnd("baz"), None): QueryProjection[HNil],
      (QueryProjectOne(QueryPathEnd("foo"), None): QueryProjection[HNil]) ::
      (QueryProjectOne(QueryPathEnd("bar"), None): QueryProjection[HNil]) ::
      HNil,
      HNil: HNil, QueryComparisonNop, List.empty, List.empty, None, None)
    )

}
