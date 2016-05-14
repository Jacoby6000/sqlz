package com.github.jacoby6000.query.dsl.weak

import org.specs2._

import scalaz.concurrent.Task
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.interpreters._
import com.github.jacoby6000.query.interpreters.sqlDialects.postgres
import com.github.jacoby6000.query.dsl.weak.sql._
import com.github.jacoby6000.query.shapeless.Typeclasses._
import _root_.shapeless._
import _root_.shapeless.ops.hlist.FlatMapper
import _root_.shapeless.ops.hlist.FlatMapper._
import doobie.imports._

/**
  * Created by jbarber on 5/14/16.
  */
class SqlDSLSimpleSelectTest extends Specification { def is = s2"""

  A simple query should
    return some results                               $result
                                                      """

  val xa = DriverManagerTransactor[Task](
    "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
  )

  import xa.yolo._

  // the below should compile.
  case class CountryCodePair(name: String, code: String)

  def result =
    (
      select(
        p"c.name" as "n",
        p"c.code" as "c"
      ) from (
        p"country" as "c"
      )
    ).build
      .query[CountryCodePair]
      .list
      .transact(xa)
      .unsafePerformSync must haveSize(239)

}
