package com.github.jacoby6000.query

import _root_.doobie.imports._
import _root_.doobie.syntax.string.Builder
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.dsl.sql._
import shapeless._

/**
  * Created by jacob.barber on 3/4/16.
  */
object doobie {

  implicit def selectBuilderToSelectQuery(queryBuilder: QueryBuilder): DoobieExpressionExtensions = new DoobieExpressionExtensions(queryBuilder.query)
  implicit def updateBuilderToUpdateQuery(updateBuilder: UpdateBuilder): DoobieExpressionExtensions = new DoobieExpressionExtensions(updateBuilder.query)
  
  implicit class DoobieExpressionExtensions(val query: QueryExpression) extends AnyVal {
    def sql: String = interpreter.interpretPSql(query)

    def prepareDynamic[A <: HList : Param](params: A): Builder[A] =
      new StringContext(sql.split('?'): _*).sql.applyProduct(params)

    /**
     * The segment below serves just to provide more convenient ways to call prepareDynamic. It is akin to shapeless' applyDynamic, but without the macros. So IDEs actually support it.
     */
    def prepare: Builder[HNil] = prepareDynamic(HNil)
    def prepare[A: Param](a: A): Builder[A :: HNil] = prepareDynamic(a :: HNil)
    def prepare[A: Param, B: Param](a: A, b: B): Builder[A :: B :: HNil] = prepareDynamic(a :: b :: HNil)
    def prepare[A: Param, B: Param, C: Param](a: A, b: B, c: C): Builder[A :: B :: C :: HNil] = prepareDynamic(a :: b :: c :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param](a: A, b: B, c: C, d: D): Builder[A :: B :: C :: D :: HNil] = prepareDynamic(a :: b :: c :: d :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param](a: A, b: B, c: C, d: D, e: E): Builder[A :: B :: C :: D :: E :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param](a: A, b: B, c: C, d: D, e: E, f: F): Builder[A :: B :: C :: D :: E :: F :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: f :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G): Builder[A :: B :: C :: D :: E :: F :: G :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: f :: g :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H): Builder[A :: B :: C :: D :: E :: F :: G :: H :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: f :: g :: h :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param, I: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I): Builder[A :: B :: C :: D :: E :: F :: G :: H :: I :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: f :: g :: h :: i :: HNil)
    def prepare[A: Param, B: Param, C: Param, D: Param, E: Param, F: Param, G: Param, H: Param, I: Param, J: Param](a: A, b: B, c: C, d: D, e: E, f: F, g: G, h: H, i: I, j: J): Builder[A :: B :: C :: D :: E :: F :: G :: H :: I :: J :: HNil] = prepareDynamic(a :: b :: c :: d :: e :: f :: g :: h :: i :: j :: HNil)
  

  }
}
