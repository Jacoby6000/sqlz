package com.github.jacoby6000.query.dsl

import com.github.jacoby6000.query.ast.{QueryInsert, QuerySelect}
import com.github.jacoby6000.query.interpreter
import shapeless.{HNil, HList}
import _root_.doobie.hi
import _root_.doobie.imports._
import _root_.doobie.syntax.string._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  class DoobieQueryBuilder[A: Composite](query: QuerySelect) {
    def prepare[B: Composite](params: B): scalaz.stream.Process[ConnectionIO, A] =
      HC.process[A](interpreter.interpretSql(query), HPS.set(params))

    def prepare: scalaz.stream.Process[ConnectionIO, A] =
      new SqlInterpolator(StringContext(interpreter.interpretSql(query))).sql.apply().query[A].process
  }

  class DoobieInsertBuilder[A: Composite](insert: QueryInsert) {
    def prepare[B: Composite](params: B)(generatedKeys: List[String]): scalaz.stream.Process[ConnectionIO, A] =
      HC.updateWithGeneratedKeys[A](generatedKeys)(interpreter.interpretSql(insert), HPS.set(params))

    def prepare(generatedKeys: List[String]): ConnectionIO[A] =
      new SqlInterpolator(StringContext(interpreter.interpretSql(insert))).sql.apply().update.withUniqueGeneratedKeys[A](generatedKeys: _*)
  }


  implicit class DoobieQueryExtensions(query: QuerySelect) {
    def apply[A: Composite] = new DoobieQueryBuilder[A](query)
  }

  implicit class DoobieInsertExtensions(query: QueryInsert) {
    def apply[A: Composite] = new DoobieInsertBuilder[A](query)
  }

}
