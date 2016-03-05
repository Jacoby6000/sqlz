package com.github.jacoby6000.query.dsl

import com.github.jacoby6000.query.ast.Query
import com.github.jacoby6000.query.interpreter
import _root_.doobie.hi
import shapeless.HList
import _root_.doobie.imports._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  class ProxyT[T]
  object ProxyT {
    def apply[T] = new ProxyT[T]
  }

  implicit class QueryExtensions(query: Query) {
    def prepare[A <: HList : Composite, B: Composite](outType: ProxyT[B])(params: A): scalaz.stream.Process[hi.ConnectionIO, B] =
      HC.process[B](interpreter.interpretSql(query), HPS.set(params))
  }

}
