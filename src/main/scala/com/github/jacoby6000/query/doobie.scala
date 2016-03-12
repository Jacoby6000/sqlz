package com.github.jacoby6000.query

import _root_.doobie.imports._
import com.github.jacoby6000.query.ast.{QueryInsert, QuerySelect}
import scalaz._, Scalaz._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit class DoobieQueryExtensions(val query: QuerySelect) extends AnyVal {
    def apply[A: Composite] = new DoobieQueryBuilder[A](query)
  }

  implicit class DoobieInsertExtensions(val query: QueryInsert) {
    def apply[A: Composite] = new DoobieInsertBuilder[A](query)
  }

  class DoobieQueryBuilder[A: Composite](query: QuerySelect) {
    def prepare[B: Composite](params: B): scalaz.stream.Process[ConnectionIO, A] =
      HC.process[A](interpreter.interpretPSql(query), HPS.set(params))

    def prepare: scalaz.stream.Process[ConnectionIO, A] =
      Query.apply[Unit, A](interpreter.interpretPSql(query)).toQuery0(()).process
  }

  class DoobieInsertBuilder[A: Composite](insert: QueryInsert) {

    def prepare[B: Composite](params: B)(generatedKeys: List[String]): ConnectionIO[A] =
      HC.prepareStatementS(interpreter.interpretPSql(insert), generatedKeys)(HPS.set(params) >> HPS.executeUpdateWithUniqueGeneratedKeys[A])

    def prepare(generatedKeys: List[String]): ConnectionIO[A] =
      HC.prepareStatementS(interpreter.interpretPSql(insert), generatedKeys)(HPS.executeUpdateWithUniqueGeneratedKeys[A])

    def prepare(implicit ev: A =:= Int): ConnectionIO[Int] =
      HC.prepareStatementS(interpreter.interpretPSql(insert), List.empty)(HPS.executeUpdate)
  }

}
