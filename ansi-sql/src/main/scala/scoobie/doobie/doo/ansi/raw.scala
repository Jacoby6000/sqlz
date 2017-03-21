package scoobie.doobie.doo.ansi

/**
  * You probably don't want this, but it's here for people who want to be able to print out their queries.
  *
  * THIS DOES NOT USE PREPARED STATEMENTS, YOU WILL BE VULNERABLE TO SQL INJECTION.
  */
object raw {

  @deprecated
  trait Showall[A] {
    def toString(a: A): String = a.toString
  }

  implicit def showAllInstance[A]: Showall[A] = new Showall[A] {}

  implicit val showAllSqlQueryLifter: SqlQueryLifter[Showall, String] =
    new SqlQueryLifter[Showall, String] {
      override def liftValue[A](a: A, fa: Showall[A]): String = fa.toString(a)
    }

  implicit val rawSqlInterpreter = SqlInterpreter("\"")
}
