

[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

### Querying with Doobie, without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

### The Sql DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```tut:silent
import com.github.jacoby6000.scoobie.interpreters.sqlDialects.postgres
import com.github.jacoby6000.scoobie.dsl.weak.sql._

val q =
  select (
    c"foo" + 10 as "woozle",
    `*`
  ) from ( 
    p"bar" 
  ) leftOuterJoin (
    c"baz" as "b" on (
      c"bar.id" === c"baz.barId"
    )
  ) innerJoin (
    c"biz" as "c" on (
      c"biz.id" === c"bar.bizId"
    ) 
  ) where (
    c"biz.name" === "LightSaber" and
    c"biz.age" > 27
  ) orderBy c"biz.age".desc groupBy c"baz.worth".asc

postgres.genSql(q.build) // Print the Postgres sql string that would be created by this query
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
        ON "bar"."id" = "baz"."barId" 
INNER JOIN
    "biz" AS c 
        ON "biz"."id" = "bar"."bizId" 
WHERE
    "biz"."name" = ?
    AND  "biz"."age" > ? 
ORDER BY
    "biz"."age" DESC 
GROUP BY
    "baz"."worth" ASC
```

As a proof of concept, here are some examples translated over from the book of doobie

First, lets set up a repl session with our imports, plus what we need to run doobie.

```tut:silent
import com.github.jacoby6000.scoobie.interpreters._ // Import the interpreters
import com.github.jacoby6000.scoobie.interpreters.sqlDialects.postgres // Use postgres
import com.github.jacoby6000.scoobie.dsl.weak.sql._ // Import the Sql-like weakly typed DSL.
import doobie.imports._ // Import doobie
import scalaz.concurrent.Task 

val xa = DriverManagerTransactor[Task](
  "org.postgresql.Driver", "jdbc:postgresql:world", "postgres", "postgres"
)

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
  (baseQuery where c"population" > n)
    .build
    .queryAndPrint[Country](sql => println("\n" + sql))
}

biggerThan(150000000).quick.unsafePerformSync


def populationIn(r: Range) = {
  (baseQuery where (
    c"population" >= r.min and
    c"population" <= r.max
  )).build
    .queryAndPrint[Country](sql => println("\n" + sql))
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
    c"country" as "c1"
  ) leftOuterJoin (
    c"country" as "c2" on (
      func"reverse"(c"c1.code") === c"c2.code"
    )
  ) where (
    (c"c2.code" !== `null`) and
    (c"c2.name" !== c"c1.name")
  )).build
    .queryAndPrint[ComplimentaryCountries](sql => println("\n" + sql))
}

joined.quick.unsafePerformSync
```
