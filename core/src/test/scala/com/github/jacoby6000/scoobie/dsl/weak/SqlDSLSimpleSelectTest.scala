package com.github.jacoby6000.scoobie.dsl.weak

import org.specs2._

import scalaz.concurrent.Task
import com.github.jacoby6000.scoobie.interpreters._
import com.github.jacoby6000.scoobie.interpreters.sqlDialects.postgres
import com.github.jacoby6000.scoobie.dsl.weak.sql._
import doobie.imports._
import shapeless.syntax.singleton._

import scalaz.NonEmptyList

/**
  * Created by jbarber on 5/14/16.
  */
class SqlDSLSimpleSelectTest extends Specification {

  val xa = DriverManagerTransactor[Task](
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
        (c"c1.gnp" + 5) as "c1gnp",
        p"c1.code",
        p"c2.name",
        p"c2.gnp",
        p"c2.code"
      ) from (
        p"country" as "c1"
      ) innerJoin (
        p"country" as "c2" on (
          c"c2.code" === func"reverse" (c"c1.code")
        )
      ) where (
        sql.not(c"c2.code" === "USA") and
        c"c1.lifeexpectancy" > 50
      )
    ).build
      .queryAndPrint[(Country, Country)](println _)
      .list
      .transact(xa)
      .unsafePerformSync

  lazy val selectWhereIn = {
    val codes = NonEmptyList("TUV", "YUG")
    implicit val codesParam = Param.many(codes)
    (
      select(p"name") from p"country" where (c"code" in ("USA", "BRA", codes.narrow))
    ).build
      .queryAndPrint[String](println _)
      .list
      .transact(xa)
      .unsafePerformSync
  }

  lazy val endToEndTest = {
    for {
      inserted <- (insertInto(c"city") values(
                    c"id" ==> 4080,
                    c"name" ==> "test",
                    c"countrycode" ==> "SHT",
                    c"district" ==> "District of unlawful testing",
                    c"population" ==> 1
                  )).updateAndPrint(println).run

      select1 <- (select(p"district") from p"city" where (c"id" === 4080))
                    .build
                    .queryAndPrint[String](println)
                    .option

      updated <- (update(c"city") set (c"population" ==> 10) where (c"id" === 4080))
                    .build
                    .updateAndPrint(println)
                    .run

      select2 <- (select(p"population") from p"city" where (c"id" === 4080))
                    .build
                    .queryAndPrint[Int](println)
                    .option

      deleted <- (deleteFrom(c"city") where (c"id" === 4080))
                    .updateAndPrint(println)
                    .run

      select3 <- (select(p"population") from p"city" where (c"id" === 4080))
                    .build
                    .queryAndPrint[Int](println)
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
