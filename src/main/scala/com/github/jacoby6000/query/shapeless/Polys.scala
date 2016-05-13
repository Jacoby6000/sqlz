package com.github.jacoby6000.query.shapeless

import com.github.jacoby6000.query.ast._
import shapeless.{ HList, Poly1 }

/**
 * Created by jacob.barber on 5/13/16.
 */
object Polys {
  object Flatten extends Poly1 {
    implicit def flattener[A <: HList] = at[A](identity)
  }
}
