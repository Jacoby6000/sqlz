package scoobie.doobie

import doobie.imports._
import scoobie.ast.coercion.Coerce
import scoobie.ast.interpreters.ansi._
import scoobie.ast.interpreters.ansi.SqlInterpreter.LiteralQueryString

/**
  * Created by jbarber on 5/20/16.
  */
object mysql extends DoobieSupport {

  implicit val fragmentLifter =
    new SqlQueryLifter[ScoobieFragmentProducer, Fragment] {
      def liftValue[A](a: A, fa: ScoobieFragmentProducer[A]): Fragment = fa.genFragment(a)
    }

  implicit val scoobieFragmentProducerForLiteralQueryString: ScoobieFragmentProducer[LiteralQueryString] =
    ScoobieFragmentProducer[LiteralQueryString](s => new StringContext(s.s).fr0.apply())

  val interpreter: SqlInterpreter[ScoobieFragmentProducer, Fragment] = SqlInterpreter[ScoobieFragmentProducer, Fragment]("`")

  implicit val doobieInterpreter = DoobieSqlInterpreter(interpreter.interpretSql(_))
  implicit val coercetoScoobieFragmentProducer: Coerce[ScoobieFragmentProducer] = scoobie.doobie.doobieCoercer

}
