[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/Jacoby6000/scoobie.svg?branch=master)](https://travis-ci.org/Jacoby6000/scoobie) [![codecov](https://codecov.io/gh/Jacoby6000/scoobie/branch/master/graph/badge.svg)](https://codecov.io/gh/Jacoby6000/scoobie)

### Querying with [Doobie](https://github.com/tpolecat/doobie), without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

### Getting Started

Add the sonatype releases resolver
```scala
  resolvers += Resolver.sonatypeRepo("releases")
```

Add this project as a dependency.
```scala
  libraryDependencies ++= {
    val scoobieVersion = "0.3.0"

    Seq(
      "com.github.jacoby6000" %% "scoobie-contrib-doobie40-postgres" % scoobieVersion, // import doobie 4.0 postgres support
      "com.github.jacoby6000" %% "scoobie-contrib-mild-sql-dsl" % scoobieVersion // import the weak sql dsl
    )
  }
```

Refer to the chart below to see what dependencies use what versions of things

| scoobie distribution              | scoobie version | doobie | status | jdk  | scala | scalaz | scalaz-stream | shapeless |
|:---------------------------------:|:---------------:|:------:|:------:|:----:|:-----:|:------:|:-------------:|:---------:|
| scoobie-contrib-doobie30-postgres | 0.2.1           |  0.3.0 | stable | 1.8+ | 2.11  |   7.2  |      0.8a     |    2.3    |
| scoobie-contrib-doobie23-postgres | 0.2.1           |  0.2.3 | stable | 1.6+ | 2.11  |   7.1  |      0.8      |    2.2    |
| scoobie-contrib-mild-sql-dsl      | 0.2.1           |  N/A   | stable | 1.6+ | 2.11  |   N/A  |      N/A      |    2.3    | 

### Using the SQL DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```tut
import scoobie.doobie.doo.postgres._
import scoobie.snacks.mild.sql._

val q =
  select (
    p"foo" + 10 as "woozle",
    `*`
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

val sql = q.build.genFragment // Generate the sql fragment associated with this query
```

The formatted output of this is

```sql
SELECT
    "foo" + ? AS woozle,
    * 
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

As a proof of concept, here are some examples translated over from the book of doobie

First, lets set up a repl session with our imports, plus what we need to run doobie.

```tut:silent
import scoobie.doobie.doo.postgres._ // Use postgres with doobie support
import scoobie.snacks.mild.sql._ // Import the Sql-like weakly (mildly) typed DSL.
import doobie.imports._ // Import doobie transactor
import scalaz.concurrent.Task 

val xa = DriverManagerTransactor[Task](
  "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
)

implicit val logger = LogHandler.jdkLogHandler

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

And now lets run some basic queries (Note, instead of `.queryAndPrint[T](printer)` you can use `.query[T]` if you do not care to see that sql being generated.) 

```tut
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

And a more complicated example

```tut
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

Check out [this end to end example](https://github.com/Jacoby6000/scoobie/blob/master/postgres/src/test/scala/scoobie/doobie/PostgresTest.scala#L71) for an idea of how to utilize insert/update/delete as well.
