package sqlz.algebra


object newts {
  trait NewTypeSyntax {
    implicit def toReprOps[A](a: A): NewType.ReprOps[A] = new NewType.ReprOps[A](a)
    implicit def toNewTypeOps[A](a: A): NewType.NewTypeOps[A] = new NewType.NewTypeOps[A](a)
  }

  trait NewType[T, R] {
    final type Type = T
    final type Repr = R

    def subst[F[_]](fa: F[Repr]): F[T]

    def unsubst[F[_]](fa: F[T]): F[Repr] = {
      type f[x] = F[x] => F[Repr]
      subst[f](identity[F[Repr]])(fa)
    }

    def wrap(r: Repr): T = {
      type f[x] = x
      subst[f](r)
    }

    def unwrap(r: T): Repr = {
      type f[x] = x
      unsubst[f](r)
    }
  }
  object NewType {
    type Aux[T, R] = NewType[T, R]

    trait CompanionBase {
      protected type Upper
      type Repr <: Upper

      type Base <: Upper
      trait Tag extends Any
      type Type <: (Base with Tag)
    }
    trait Companion[U, R <: U] extends CompanionBase {
      final type Upper = U
      final type Repr = R
    }

    trait Default { self: CompanionBase =>
      def apply(value: Repr): Type =
        newType.wrap(value)

      def unapply(value: Type): Some[Repr] =
        Some(newType.unwrap(value))

      implicit val newType: NewType.Aux[Type, Repr] = new NewType[Type, Repr] {
        //      type Repr = self.Repr
        def subst[F[_]](fa: F[Repr]): F[Type] = fa.asInstanceOf[F[Type]]
      }
    }

    trait Of[R]              extends Companion[Any,    R] with Default
    trait TranslucentOf[R]   extends Companion[R,      R] with Default
    trait OfRef[R <: AnyRef] extends Companion[AnyRef, R] with Default

    trait Refined[R] extends Companion[R, R] {
      def isValid(r: R): Boolean

      def apply(value: R): Option[Type] =
        if (isValid(value)) Some(value.asInstanceOf[Type])
        else None
    }

    final class ReprOps[R](val value: R) extends AnyVal {
      def nu[T](implicit N: NewType[T, R]): T = N.wrap(value)
    }
    final class NewTypeOps[T](val value: T) extends AnyVal {
      def un[R](implicit N: NewType.Aux[T, R]): R = N.unwrap(value)
    }
  }
}
