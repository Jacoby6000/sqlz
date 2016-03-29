[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Code samples verified by Tut](https://img.shields.io/badge/tut-verified-brightgreen.svg)](https://github.com/tpolecat/tut)

### Querying with Doobie, without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

### The Sql DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```scala
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

```scala
import com.github.jacoby6000.query.ast._
import com.github.jacoby6000.query.doobie._
import com.github.jacoby6000.query.dsl.sql._
import com.github.jacoby6000.query.dsl.sql.implicitConversions._
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

```scala
scala> def biggerThan(n: Int) = {
     |   (baseQuery where p"population" > `?`)
     |     .query
     |     .prepare(n :: HNil)
     |     .query[Country]
     | }
biggerThan: (n: Int)doobie.util.query.Query0[Country]

scala> biggerThan(150000000).quick.run
  Country(BRA,Brazil,170115000,Some(776739.0))
  Country(IDN,Indonesia,212107000,Some(84982.0))
  Country(IND,India,1013662000,Some(447114.0))
  Country(CHN,China,1277558000,Some(982268.0))
  Country(PAK,Pakistan,156483000,Some(61289.0))
  Country(USA,United States,278357000,Some(8510700.0))

scala> def populationIn(r: Range) = {
     |   (baseQuery where (
     |     p"population" >= `?` and
     |     p"population" <= `?`
     |   )).query
     |     .prepare(r.min :: r.max :: HNil)
     |     .query[Country]
     | } 
populationIn: (r: Range)doobie.util.query.Query0[Country]

scala> populationIn(150000000 to 200000000).quick.run
  Country(BRA,Brazil,170115000,Some(776739.0))
  Country(PAK,Pakistan,156483000,Some(61289.0))
```

And a more complicated example

```scala
scala> case class ComplimentaryCountries(code1: String, name1: String, code2: String, name2: String)
defined class ComplimentaryCountries

scala> def joined = {
     |   (select(
     |     p"c1.code",
     |     p"c1.name",
     |     p"c2.code",
     |     p"c2.name"
     |   ) from (
     |     p"country" as "c1"
     |   ) leftOuterJoin (
     |     p"country" as "c2"
     |   ) on (
     |     func"reverse"(p"c1.code") === p"c2.code"
     |   ) where (
     |     (p"c2.code" !== `null`) and
     |     (p"c2.name" !== p"c1.name")
     |   )).query
     |     .prepare
     |     .query[ComplimentaryCountries] 
     | }
joined: doobie.util.query.Query0[ComplimentaryCountries]

scala> joined.quick.run
  ComplimentaryCountries(PSE,Palestine,ESP,Spain)
  ComplimentaryCountries(YUG,Yugoslavia,GUY,Guyana)
  ComplimentaryCountries(ESP,Spain,PSE,Palestine)
  ComplimentaryCountries(SUR,Suriname,RUS,Russian Federation)
  ComplimentaryCountries(RUS,Russian Federation,SUR,Suriname)
  ComplimentaryCountries(VUT,Vanuatu,TUV,Tuvalu)
  ComplimentaryCountries(TUV,Tuvalu,VUT,Vanuatu)
  ComplimentaryCountries(GUY,Guyana,YUG,Yugoslavia)
```
