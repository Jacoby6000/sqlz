package com.github.jacoby6000.query.dsl.weak

import org.specs2._

import scalaz.concurrent.Task
import com.github.jacoby6000.query.interpreters._
import com.github.jacoby6000.query.interpreters.sqlDialects.postgres
import com.github.jacoby6000.query.dsl.weak.sql._
import doobie.imports._

/**
  * Created by jbarber on 5/14/16.
  */
class SqlDSLSimpleSelectTest extends Specification {
  def is =
    s2"""

  A simple query should
    return some results                               $testResult
                                                      """

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

  case class Country(name: String, gnp: Int, code: String)

  lazy val result =
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

  def testResult = {
    println(result.mkString("\n"))
    result must haveSize(10)
  }
}
