package com.github.jacoby6000.query

import _root_.doobie.imports._
import com.github.jacoby6000.query.ast._
import scalaz._, Scalaz._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit class DoobieQueryExtensions(val query: QuerySelect) extends AnyVal {
    def apply[A: Composite]: DoobieQueryBuilder[A] = DoobieQueryBuilder[A](query)
    def doobie[A: Composite]: DoobieQueryBuilder[A] = apply[A]
  }

  implicit class DoobieModifyExtensions(val query: QueryModify) {
    def apply[A: Composite]: DoobieUpdateBuilder[A] = DoobieUpdateBuilder[A](query)
    def doobie[A: Composite]: DoobieUpdateBuilder[A] = apply[A]
  }

  case class DoobieQueryBuilder[A: Composite](query: QuerySelect) {
    def prepare[B: Composite](params: B): scalaz.stream.Process[ConnectionIO, A] =
      HC.process[A](interpreter.interpretPSql(query), HPS.set(params))

    def prepare: scalaz.stream.Process[ConnectionIO, A] =
      Query.apply[Unit, A](interpreter.interpretPSql(query)).toQuery0(()).process
  }

  case class DoobieUpdateBuilder[A: Composite](update: QueryModify) {
    def prepare[B: Composite](params: B)(generatedKeys: List[String]): ConnectionIO[A] =
      HC.prepareStatementS(interpreter.interpretPSql(update), generatedKeys)(HPS.set(params) >> HPS.executeUpdateWithUniqueGeneratedKeys[A])

    def prepare(generatedKeys: List[String]): ConnectionIO[A] =
      HC.prepareStatementS(interpreter.interpretPSql(update), generatedKeys)(HPS.executeUpdateWithUniqueGeneratedKeys[A])

    def prepare(implicit ev: A =:= Int): ConnectionIO[Int] =
      HC.prepareStatementS(interpreter.interpretPSql(update), List.empty)(HPS.executeUpdate)
  }

  case class DoobieDeleteBuilder(delete: QueryDelete) {
    def prepare[B: Composite](params: B): ConnectionIO[Int] =
      HC.prepareStatementS[Int](interpreter.interpretPSql(delete), List.empty)(HPS.executeUpdate)

    def prepare: ConnectionIO[Int] =
      HC.prepareStatementS(interpreter.interpretPSql(delete), List.empty)(HPS.executeUpdate)
  }
}
