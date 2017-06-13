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
      "com.github.jacoby6000" %% "scoobie-contrib-doobie41-postgres" % scoobieVersion, // import doobie 4.1 with postgres support
      "com.github.jacoby6000" %% "scoobie-contrib-mild-sql-dsl" % scoobieVersion // import the weak sql dsl
    )
  }
```

Refer to the chart below to see what dependencies use what versions of things

| scoobie distribution              | scoobie version | doobie | status | jdk  | scala          | scalaz | scalaz-stream  | shapeless |
|:---------------------------------:|:---------------:|:------:|:------:|:----:|:--------------:|:------:|:--------------:|:---------:|
| scoobie-contrib-doobie40-postgres | 0.3.0           |  0.4.0 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie41-postgres | 0.3.0           |  0.4.1 | stable | 1.6+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie40-mysql    | 0.3.0           |  0.4.0 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie41-mysql    | 0.3.0           |  0.4.1 | stable | 1.6+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-mild-sql-dsl      | 0.3.0           |  N/A   | stable | 1.6+ | 2.11.8/2.12.1  |   N/A  |      N/A       |    N/A    |

### Using the SQL DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```scala
scala> import scoobie.doobie.doo.postgres._
import scoobie.doobie.doo.postgres._

scala> import scoobie.snacks.mild.sql._
import scoobie.snacks.mild.sql._

scala> val q =
     |   select (
     |     p"foo" + 10 as "woozle",
     |     `*`
     |   ) from ( 
     |     p"bar" 
     |   ) leftOuterJoin (
     |     p"baz" as "b" on (
     |       p"bar.id" === p"b.barId"
     |     )
     |   ) innerJoin (
     |     p"biz" as "c" on (
     |       p"c.id" === p"bar.bizId"
     |     ) 
     |   ) where (
     |     p"c.name" === "LightSaber" and
     |     p"c.age" > 27
     |   ) orderBy p"c.age".desc groupBy p"b.worth".asc
q: scoobie.snacks.mild.sql.QueryBuilder[scoobie.doobie.ScoobieFragmentProducer] = QueryBuilder(QueryProjectOne(QueryPathEnd(bar),None),List(QueryProjectOne(QueryAdd(QueryPathEnd(foo),QueryParameter(10)),Some(woozle)), QueryProjectAll),List(QueryInnerJoin(QueryProjectOne(QueryPathEnd(biz),Some(c)),QueryEqual(QueryPathCons(c,QueryPathEnd(id)),QueryPathCons(bar,QueryPathEnd(bizId)))), QueryLeftOuterJoin(QueryProjectOne(QueryPathEnd(baz),Some(b)),QueryEqual(QueryPathCons(bar,QueryPathEnd(id)),QueryPathCons(b,QueryPathEnd(barId))))),QueryAnd(QueryComparisonNop,QueryAnd(QueryEqual(QueryPathCons(c,QueryPathEnd(name)),QueryParameter(LightSaber)),QueryGreaterThan(QueryPathCons(c,QueryPathEnd(age)),QueryParameter(27)))),List(QuerySortDesc(QueryPathCons(c,QueryPathEnd(age)))),List(QuerySortAsc(Que...

scala> val sql = q.build.genFragment // Generate the sql fragment associated with this query
sql: doobie.imports.Fragment = Fragment("SELECT "foo"  +  ? AS woozle , *FROM "bar" INNER JOIN "biz" AS c ON "c"."id"  =  "bar"."bizId" LEFT OUTER JOIN "baz" AS b ON "bar"."id"  =  "b"."barId" WHERE "c"."name"  =  ?  AND  "c"."age"  >  ? ORDER BY "c"."age"  DESC GROUP BY "b"."worth"  ASC ")
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

```scala
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

```scala
scala> def biggerThan(n: Int) = {
     |   (baseQuery where p"population" > n)
     |     .build
     |     .query[Country]
     | }
biggerThan: (n: Int)doobie.imports.Query0[Country]

scala> biggerThan(150000000).quick.unsafePerformSync
  Country(BRA,Brazil,170115000,Some(776739.0))
  Country(IDN,Indonesia,212107000,Some(84982.0))
  Country(IND,India,1013662000,Some(447114.0))
  Country(CHN,China,1277558000,Some(982268.0))
  Country(PAK,Pakistan,156483000,Some(61289.0))
  Country(USA,United States,278357000,Some(8510700.0))

scala> def populationIn(r: Range) = {
     |   (baseQuery where (
     |     p"population" >= r.min and
     |     p"population" <= r.max
     |   )).build
     |     .query[Country]
     | } 
populationIn: (r: Range)doobie.imports.Query0[Country]

scala> populationIn(150000000 to 200000000).quick.unsafePerformSync
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
     |   ) innerJoin (
     |     p"country" as "c2" on (
     |       func"reverse"(p"c1.code") === p"c2.code"
     |     )
     |   ) where (
     |     p"c2.name" !== p"c1.name"
     |   )).build
     |     .query[ComplimentaryCountries]
     | }
joined: doobie.imports.Query0[ComplimentaryCountries]

scala> joined.quick.unsafePerformSync
  ComplimentaryCountries(PSE,Palestine,ESP,Spain)
  ComplimentaryCountries(YUG,Yugoslavia,GUY,Guyana)
  ComplimentaryCountries(ESP,Spain,PSE,Palestine)
  ComplimentaryCountries(SUR,Suriname,RUS,Russian Federation)
  ComplimentaryCountries(RUS,Russian Federation,SUR,Suriname)
  ComplimentaryCountries(VUT,Vanuatu,TUV,Tuvalu)
  ComplimentaryCountries(TUV,Tuvalu,VUT,Vanuatu)
  ComplimentaryCountries(GUY,Guyana,YUG,Yugoslavia)
```

Check out [this end to end example](https://github.com/Jacoby6000/scoobie/blob/master/postgres/src/test/scala/scoobie/doobie/PostgresTest.scala#L71) for an idea of how to utilize insert/update/delete as well.
