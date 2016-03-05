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

  class Builder[A: Composite](query: Query) {
    def prepare[B: Composite](params: B) =
      HC.process[A](interpreter.interpretSql(query), HPS.set(params))
  }

  implicit class QueryExtensions(query: Query) {
    def apply[A: Composite] = new Builder[A](query)
  }

}
