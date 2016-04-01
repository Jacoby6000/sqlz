

[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

### Querying with Doobie, without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

### The Sql DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```tut:silent
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.interpreter
import com.github.jacoby6000.query.dsl.sql._

val q =
  select (
    p"foo" ++ 10 as "woozle",
    `*`
  ) from p"bar" leftOuterJoin (
    p"baz" as "b" 
  ) on (
    p"bar.id" === p"baz.barId"
  ) innerJoin (
    p"biz" as "c" 
  ) on (
    p"biz.id" === p"bar.bizId"
  ) where (
    p"biz.name" === "LightSaber" and
    p"biz.age" > 27
  ) orderBy p"biz.age".desc groupBy p"baz.worth".asc

interpreter.interpretPSql(q.query) // Print the Postgres sql string that would be created by this query
```

The formatted output of this is

```sql
SELECT
    "foo" + 10 AS woozle,
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
    "biz"."name" = 'LightSaber'  
    AND  "biz"."age" > 27 
ORDER BY
    "biz"."age" DESC 
GROUP BY
    "baz"."worth" ASC
```

As a proof of concept, here are some examples translated over from the book of doobie

First, lets set up a repl session with our imports, plus what we need to run doobie.

```tut:silent
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.doobie.postgres._
import com.github.jacoby6000.query.dsl.sql._
import doobie.imports._
import shapeless.HNil
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

And now lets run some basic queries

```tut
def biggerThan(n: Int) = {
  (baseQuery where p"population" > `?`)
    .prepare(n)
    .query[Country]
}

biggerThan(150000000).quick.run


def populationIn(r: Range) = {
  (baseQuery where (
    p"population" >= `?` and
    p"population" <= `?`
  )).prepare(r.min, r.max)
    .query[Country]
} 

populationIn(150000000 to 200000000).quick.run
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
  ) leftOuterJoin (
    p"country" as "c2"
  ) on (
    func"reverse"(p"c1.code") === p"c2.code"
  ) where (
    (p"c2.code" !== `null`) and
    (p"c2.name" !== p"c1.name")
  )).prepare
    .query[ComplimentaryCountries] 
}

joined.quick.run
```
