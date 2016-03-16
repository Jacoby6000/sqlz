package com.github.jacoby6000.query

import _root_.doobie.imports._
import _root_.doobie.syntax.string.Builder
import com.github.jacoby6000.query.ast._
import shapeless.{HNil, HList}
import scalaz._, Scalaz._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit def compositeDerivedFromParam[A](implicit ev: Param[A]): Composite[A] = ev.composite

  implicit class DoobieExpressionExtensions(val query: QuerySelect) extends AnyVal {
    def sql: String = interpreter.interpretPSql(query)

    def prepare[A <: HList : Param](params: A): Builder[A] =
      new StringContext(sql.split("?"): _*).sql.applyProduct(params)

    def prepare: Builder[HNil] =
      new StringContext(sql).sql.applyProduct(HNil)
  }
}
