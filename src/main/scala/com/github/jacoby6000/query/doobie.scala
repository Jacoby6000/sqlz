package com.github.jacoby6000.query

import _root_.doobie.imports._
import _root_.doobie.syntax.string.Builder
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.dsl.sql._
import shapeless.{HNil, HList}

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit def selectBuilderToSelectQuery(queryBuilder: QueryBuilder): DoobieExpressionExtensions = new DoobieExpressionExtensions(queryBuilder.query)
  implicit def updateBuilderToUpdateQuery(updateBuilder: UpdateBuilder): DoobieExpressionExtensions = new DoobieExpressionExtensions(updateBuilder.query)
  
  implicit class DoobieExpressionExtensions(val query: QueryExpression) extends AnyVal {
    def sql: String = interpreter.interpretPSql(query)

    def prepare[A <: HList : Param](params: A): Builder[A] =
      new StringContext(sql.split('?'): _*).sql.applyProduct(params)

    def prepare: Builder[HNil] =
      new StringContext(sql).sql.applyProduct(HNil)
  }
}
