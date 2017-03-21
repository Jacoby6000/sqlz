package scoobie.doobie

import doobie.imports.Fragment

trait ScoobieFragmentProducer[A] {
  def genFragment(a: A): Fragment
}

object ScoobieFragmentProducer {
  def apply[A](f: A => Fragment): ScoobieFragmentProducer[A] = new ScoobieFragmentProducer[A] {
    def genFragment(a: A): Fragment = f(a)
  }
}


