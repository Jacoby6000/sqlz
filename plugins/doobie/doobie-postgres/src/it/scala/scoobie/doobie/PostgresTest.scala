package scoobie.doobie.doo

import _root_.doobie.imports._
import doobie.specs2.imports._
import scoobie.doobie.doo.postgres._
import scoobie.snacks.mild.sql._
import org.specs2.mutable.Specification

import scalaz.NonEmptyList

/**
  * Created by jbarber on 5/14/16.
  */
object PostgresTest extends Specification with AnalysisSpec {

  implicit val logger = scoobie.doobie.log.verboseTestLogger

  override val transactor: Transactor[IOLite] = DriverManagerTransactor[IOLite](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

  case class Country(name: String, gnp: Option[BigDecimal], code: String)

  lazy val semiComplexSelectQuery: Query0[(Country, Country)] =
    (
      select (
        p"c1.name",
        (p"c1.gnp" + BigDecimal(5)) as "c1gnp",
        p"c1.code",
        p"c2.name",
        p"c2.gnp",
        p"c2.code"
      ) from (p"country" as "c1")
        innerJoin (
          p"country" as "c2" on (
            p"c2.code" === func"reverse" (p"c1.code")
          )
        )
        where (
          (p"c2.code" !== "USA")
            and
          (p"c1.lifeexpectancy" > 50F)
        )
        orderBy p"c1.name".asc
        limit 5
        offset 2
    ).build.query[(Country, Country)]


  def selectWhereInQuery(codes: NonEmptyList[String]): Query0[String] =
    (
      select ( p"name" )
        from p"country"
        where (p"code" in ("USA", "BRA", codes))
    ).build.query[String]

  lazy val insertCityQuery: Update0 =
    (
      insertInto(p"city") values(
        p"id" ==> 4080,
        p"name" ==> "test",
        p"countrycode" ==> "SHT",
        p"district" ==> "District of unlawful testing",
        p"population" ==> 1
      )
    ).build.update

  lazy val selectDistrictQuery: Query0[String] =
    (
      select ( p"district" )
        from p"city"
        where (p"id" === 4080)
    ).build.query[String]

  lazy val updateCityPopulationQuery: Update0 =
    (
      update ( p"city" )
        set (p"population" ==> 10)
        where (p"id" === 4080)
    ).build.update

  lazy val selectCityPopulationQuery: Query0[Int] =
    (
      select ( p"population" )
        from p"city"
        where (p"id" === 4080)
    ).build.query[Int]

  lazy val deleteCityQuery: Update0 =
    (
      deleteFrom ( p"city" )
        where (p"id" === 4080)
    ).build.update

  "Built Queries" should {
    "pass type checks" in {
      "semi-complex select" in check(semiComplexSelectQuery)
      "select where in" in check(selectWhereInQuery(NonEmptyList("a", "b")))
      "insert city" in check(insertCityQuery)
      "select district" in check(selectDistrictQuery)
      "update population" in check(updateCityPopulationQuery)
      "select population" in check(selectCityPopulationQuery)
      "delete city" in check(deleteCityQuery)
    }
  }

  "Running Built Queries" should {
    "generate correct results" in {
      "semi-complex select" in {
        val result = semiComplexSelectQuery
          .list
          .transact(transactor)
          .unsafePerformIO

          result must haveSize(5)
          result.headOption.map(_._1.code) must beSome("GUY")
          result.headOption.map(_._2.code) must beSome("YUG")
          result.lastOption.map(_._1.code) must beSome("SUR")
          result.lastOption.map(_._2.code) must beSome("RUS")
      }
      "select where in" in {
        val result =
          selectWhereInQuery(NonEmptyList("TUV", "YUG"))
            .list
            .transact(transactor)
            .unsafePerformIO

        result must haveSize(4)
      }
      "end to end" in {
        (for {
          inserted <- insertCityQuery.run
          select1 <- selectDistrictQuery.option
          updated <- updateCityPopulationQuery.run
          select2 <- selectCityPopulationQuery.option
          deleted <- deleteCityQuery.run
          select3 <- selectCityPopulationQuery.option
        } yield {
          inserted must beEqualTo(1)
          select1  must beSome("District of unlawful testing")
          updated  must beEqualTo(1)
          select2  must beSome(10)
          deleted  must beEqualTo(1)
          select3  must beNone
        }).transact(transactor).unsafePerformIO
      }
    }
  }
}
