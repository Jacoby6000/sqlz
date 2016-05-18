package com.github.jacoby6000.query.dsl.weak

import org.specs2._

import scalaz.concurrent.Task
import com.github.jacoby6000.query.interpreters._
import com.github.jacoby6000.query.interpreters.sqlDialects.postgres
import com.github.jacoby6000.query.dsl.weak.sql._
import doobie.imports._
import shapeless.syntax.singleton._

import scalaz.NonEmptyList

/**
  * Created by jbarber on 5/14/16.
  */
class SqlDSLSimpleSelectTest extends Specification {
  def is =
    s2"""

  Building queries should work properly
    semi-complex select                               $semiComplexSelectResult
    where in select                                   $selectWhereInResult
                                                      """

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

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

  def semiComplexSelectResult = {
    semiComplexSelect must haveSize(10)
  }

  def selectWhereInResult = {
    selectWhereIn must haveSize(4)
  }
}
