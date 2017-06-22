---
title: Quickstart
layout: docs
section: docs
---

### Getting Started

In the quick start samples below, we will be using doobie 0.4.1 with postgres and the sql dsl.

### Artifacts 

Add the sonatype releases resolver
```scala
resolvers += Resolver.sonatypeRepo("releases")
```

Add this project as a dependency.
```scala
libraryDependencies ++= {
  val scoobieVersion = "0.3.0"

  Seq(
    "com.github.jacoby6000" %% "scoobie-contrib-doobie41-postgres" % scoobieVersion, // import doobie 4.1 with postgres support
    "com.github.jacoby6000" %% "scoobie-contrib-mild-sql-dsl" % scoobieVersion // import the weak sql dsl
  )
}
```

### Using the SQL DSL

Import the scoobie DSL. DSLs will exist under the `scoobie.snacks` package.

```tut:book
import scoobie.snacks.mild.sql._
import scoobie.doobie.doo.postgres._

val q =
  select (
    p"foo" + 10 as "woozle",
    p"c.age",
    p"b.worth"
  ) from (
    p"bar"
  ) leftOuterJoin (
    p"baz" as "b" on (
      p"bar.id" === p"b.barId"
    )
  ) innerJoin (
    p"biz" as "c" on (
      p"c.id" === p"bar.bizId"
    )
  ) where (
    p"c.name" === "LightSaber" and
    p"c.age" > 27
  ) orderBy p"c.age".desc groupBy p"b.worth".asc
```

After this, you will have a `QueryBuilder` which you must transform in to a `QueryExpression`.  This is done via `q.build`

```tut:book
val queryExpression = q.build
```

After obtaining the query expression, we can interpret the sql to build a `doobie.imports.Fragment`, or go directly in to a `Query0` from doobie.
To be able to interpret the expression, we must import an interpreter (this has already been done above) 
Interpreters exist inside of `scoobie.doobie.doo.<database backend>`

```tut:book
val doobieFragment = queryExpression.genFragment
val doobieQuery = queryExpression.query[(Int, Int, Int)]
```

Both of the above are queries eqivelant to the SQL below

```sql
SELECT
    "foo" + ? AS woozle,
    "c.age",
    "b.worth"
FROM
    "bar"
LEFT OUTER JOIN
    "baz" AS b
        ON "bar"."id" = "b"."barId"
INNER JOIN
    "biz" AS c
        ON "c"."id" = "bar"."bizId"
WHERE
    "c"."name" = ?
    AND  "c"."age" > ?
ORDER BY
    "c"."age" DESC
GROUP BY
    "b"."worth" ASC
```

To see how this might interact with a real database, here are some examples copied over from the [Book Of Doobie](https://tpolecat.github.io/doobie-scalaz-0.4.0/04-Selecting.html).

First, lets set up a repl session with our imports, plus what we need to run doobie.

```tut:silent:reset
import scoobie.doobie.doo.postgres._ // Use postgres with doobie support
import scoobie.snacks.mild.sql._ // Import the Sql-like weakly (mildly) typed DSL.
import doobie.imports._ // Import doobie transactors and meta instances
import scalaz.concurrent.Task

val xa = DriverManagerTransactor[Task](
  "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
)

// queries will be logged using this logger. This logger is only accesible in test packages. You should not try to import it.  Make your own logger!
implicit val logger = scoobie.doobie.log.verboseTestLogger 

import xa.yolo._

case class Country(code: String, name: String, pop: Int, gnp: Option[Double])

val baseQuery =
  select(
    p"code",
    p"name",
    p"population",
    p"gnp"
  ) from p"country"
```

And now lets run some basic queries

```tut:book
def biggerThan(n: Int) = {
  (baseQuery where p"population" > n)
    .build
    .query[Country]
}

biggerThan(150000000).quick.unsafePerformSync


def populationIn(r: Range) = {
  (baseQuery where (
    p"population" >= r.min and
    p"population" <= r.max
  )).build
    .query[Country]
}

populationIn(150000000 to 200000000).quick.unsafePerformSync
```

And a more contrived and complicated example

```tut:book
case class ComplimentaryCountries(code1: String, name1: String, code2: String, name2: String)

def joined = {
  (select(
    p"c1.code",
    p"c1.name",
    p"c2.code",
    p"c2.name"
  ) from (
    p"country" as "c1"
  ) innerJoin (
    p"country" as "c2" on (
      func"reverse"(p"c1.code") === p"c2.code"
    )
  ) where (
    p"c2.name" !== p"c1.name"
  )).build
    .query[ComplimentaryCountries]
}

joined.quick.unsafePerformSync
```
