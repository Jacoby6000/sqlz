package com.github.jacoby6000.query

import _root_.doobie.imports._
import _root_.doobie.syntax.string.Builder
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.dsl.sql._
import com.github.jacoby6000.query.interpreters.SqlInterpreter
import shapeless._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit def selectBuilderToSelectQuery(queryBuilder: QueryBuilder)(implicit interpreter: SqlInterpreter): DoobieExpressionExtensions = new DoobieExpressionExtensions(queryBuilder.query)
  implicit def updateBuilderToUpdateQuery(updateBuilder: UpdateBuilder)(implicit interpreter: SqlInterpreter): DoobieExpressionExtensions = new DoobieExpressionExtensions(updateBuilder.query)

  implicit class DoobieExpressionExtensions(val query: QueryExpression)(implicit interpreter: SqlInterpreter) {
    def sql: String = interpreter.interpret(query)

    def prepareProduct[A <: HList : Param](params: A): Builder[A] =
      new StringContext(sql.split('?'): _*).sql.applyProduct(params)

    /**
      * The segment below serves just to provide more convenient ways to call prepareProduct. It is akin to shapeless' applyProduct, but without the macros. So IDEs actually support it.
      */
    def prepare: Builder[HNil] = prepareProduct(HNil)
    def prepare[A: Param](a: A): Builder[A :: HNil] = prepareProduct(a :: HNil)
    def prepare[A: Param, B: Param](a: A, b: B): Builder[A :: B :: HNil] = prepareProduct(a :: b :: HNil)
    def prepare[A: Param, B: Param, C: Param](a: A, b: B, c: C): Builder[A :: B :: C :: HNil] = prepareProduct(a :: b :: c :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param](a: A, b: B, c: C, d: D): Builder[A :: B :: C :: D :: HNil] = prepareProduct(a :: b :: c :: d :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param](a: A, b: B, c: C, d: D, e: E): Builder[A :: B :: C :: D :: E :: HNil] = prepareProduct(a :: b :: c :: d :: e :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param](a: A, b: B, c: C, d: D, e: E, f: F): Builder[A :: B :: C :: D :: E :: F :: HNil] = prepareProduct(a :: b :: c :: d :: e :: f :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G): Builder[A :: B :: C :: D :: E :: F :: G :: HNil] = prepareProduct(a :: b :: c :: d :: e :: f :: g :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H): Builder[A :: B :: C :: D :: E :: F :: G :: H :: HNil] = prepareProduct(a :: b :: c :: d :: e :: f :: g :: h :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param, I: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I): Builder[A :: B :: C :: D :: E :: F :: G :: H :: I :: HNil] = prepareProduct(a :: b :: c :: d :: e :: f :: g :: h :: i :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param, I: Param, J: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J): Builder[A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: HNil] = prepareProduct(a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: HNil)
  }
}
