package com.github.jacoby6000.query.dsl.weak

/**
  * Created by jbarber on 5/14/16.
  */
class SqlDSLCompileTests {
  import com.github.jacoby6000.query.ast._
  import com.github.jacoby6000.query.interpreters._
  import com.github.jacoby6000.query.interpreters.sqlDialects.postgres
  import com.github.jacoby6000.query.dsl.weak.sql._
  import com.github.jacoby6000.query.shapeless.Typeclasses._
  import _root_.shapeless._
  import _root_.shapeless.ops.hlist.FlatMapper
  import _root_.shapeless.ops.hlist.FlatMapper._

  // the below should compile.
  (
    select(
      p"foo" -- 5 as "bar"
    ) from (
      p"bar"
    )
  ).build.query[String]

}
