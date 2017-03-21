package scoobie.doobie.doo

import scoobie._
import _root_.doobie.imports._
import _root_.shapeless.syntax.singleton._
import org.specs2._
import scoobie.doobie.doo.postgres._
import scoobie.doobie.coerceToDoobieParam
import scoobie.snacks.mild.sql
import scoobie.snacks.mild.sql._
import _root_.doobie.syntax.process._

import scalaz.NonEmptyList
import scalaz.concurrent.Task

/**
  * Created by jbarber on 5/14/16.
  */
class PostgresTest extends Specification {

  implicit val logger = LogHandler.jdkLogHandler


  val xa: Transactor[Task] = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

  def is =
    s2"""

  Building queries should work properly
    semi-complex select                               $semiComplexSelectResult
    where in select                                   $selectWhereInResult
    record life-cycle                                 ${endToEndTest.transact(xa).unsafePerformSync}
                                                      """

  case class Country(name: String, gnp: Int, code: String)

  lazy val semiComplexSelect =
    (
      select(
        p"c1.name",
        (p"c1.gnp" + 5) as "c1gnp",
        p"c1.code",
        p"c2.name",
        p"c2.gnp",
        p"c2.code"
      ) from (
        p"country" as "c1"
      ) innerJoin (
        p"country" as "c2" on (
          p"c2.code" === func"reverse" (p"c1.code")
        )
      ) where (
        sql.not(p"c2.code" === "USA") and
        p"c1.lifeexpectancy" > 50
      )
    ).build
      .query[(Country, Country)]
      .list
      .transact(xa)
      .unsafePerformSync

  lazy val selectWhereIn = {
    val codes = NonEmptyList("TUV", "YUG")
    (
      select(p"name") from p"country" where (p"code" in ("USA", "BRA", codes))
    ).build
      .query[String]
      .list
      .transact(xa)
      .unsafePerformSync
  }

  lazy val endToEndTest = {
    for {
      inserted <- (insertInto(p"city") values(
                    p"id" ==> 4080,
                    p"name" ==> "test",
                    p"countrycode" ==> "SHT",
                    p"district" ==> "District of unlawful testing",
                    p"population" ==> 1
                  )).update.run

      select1 <- (select(p"district") from p"city" where (p"id" === 4080))
                    .build
                    .query[String]
                    .option

      updated <- (update(p"city") set (p"population" ==> 10) where (p"id" === 4080))
                    .build
                    .update
                    .run

      select2 <- (select(p"population") from p"city" where (p"id" === 4080))
                    .build
                    .query[Int]
                    .option

      deleted <- (deleteFrom(p"city") where (p"id" === 4080))
                    .update
                    .run

      select3 <- (select(p"population") from p"city" where (p"id" === 4080))
                    .build
                    .query[Int]
                    .option
    } yield {
      inserted must beEqualTo(1)
      select1  must beEqualTo(Some("District of unlawful testing"))
      updated  must beEqualTo(1)
      select2  must beEqualTo(Some(10))
      deleted  must beEqualTo(1)
      select3  must beEqualTo(None)
    }
  }

//    id integer NO
//    name varchar
//    countrycode c
//    district varc
//    population in

  def semiComplexSelectResult = {
    semiComplexSelect must haveSize(10)
  }

  def selectWhereInResult = {
    selectWhereIn must haveSize(4)
  }
}
