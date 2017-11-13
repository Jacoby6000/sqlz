package scoobie

import _root_.doobie.imports._
import scoobie.ast.coercion.Coerce

import scalaz._, Scalaz._
/**
  * Created by jacob.barber on 5/25/16.
  */
package object doobie {
  val doobieCoercer: Coerce[ScoobieFragmentProducer] = Coerce[ScoobieFragmentProducer]

  implicit def producerFromMeta[A: Meta]: ScoobieFragmentProducer[A] =
    ScoobieFragmentProducer[A](a => fr"$a")

  implicit def producerFromFoldable[F[_]: Foldable: Functor, A: Meta]: ScoobieFragmentProducer[F[A]] =
    ScoobieFragmentProducer[F[A]](_.map(x => fr0"$x").foldSmash(fr0"", fr0" , ", fr0""))
}
