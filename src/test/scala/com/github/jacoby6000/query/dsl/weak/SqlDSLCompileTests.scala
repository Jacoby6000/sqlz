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
class SqlDSLSimpleSelectTest extends Specification { def is = s2"""

  A simple query should
    return some results                               $testResult
                                                      """

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

  // the below should compile.
  case class CountryCodePair(gnp: Int, code: String)

  lazy val result =
    (
      select(
        (path"c1.gnp" ++ 5) as "c1name",
        p"c1.code",
        p"c2.gnp",
        p"c2.name"
      ) from (
        p"country" as "c1"
      ) innerJoin (
        p"country" as "c2"
      ) on (
        path"c2.code" === func"reverse"(c"c1.code")
      )
    ).build
      .query[(CountryCodePair, CountryCodePair)]
      .list
      .transact(xa)
      .unsafePerformSync



  def testResult = {
    println(result.mkString("\n"))
    result must haveSize(12)
  }
}
