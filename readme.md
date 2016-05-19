[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build Status](https://travis-ci.org/Jacoby6000/scoobie.svg?branch=master)](https://travis-ci.org/Jacoby6000/scoobie)

### Querying with [Doobie](https://github.com/tpolecat/doobie), without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

### Getting Started

Add the sonatype snapshot resolver
```scala
  resolvers += Resolver.sonatypeRepo("snapshots")
```

Add this project as a dependency.
```scala
  libraryDependencies += "com.github.jacoby6000" %% "scoobie" % "0.1.0-SNAPSHOT",
```

### Using the SQL DSL

Below is a sample query that somebody may want to write. The query below is perfectly valid; try it out!

```scala
scala> import com.github.jacoby6000.scoobie.interpreters._
import com.github.jacoby6000.scoobie.interpreters._

scala> import com.github.jacoby6000.scoobie.interpreters.sqlDialects.postgres
import com.github.jacoby6000.scoobie.interpreters.sqlDialects.postgres

scala> import com.github.jacoby6000.scoobie.dsl.weak.sql._
import com.github.jacoby6000.scoobie.dsl.weak.sql._

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
q: com.github.jacoby6000.scoobie.dsl.weak.sql.QueryBuilder[shapeless.HNil,shapeless.::[com.github.jacoby6000.scoobie.ast.QueryProjection[shapeless.::[Int,shapeless.HNil]],shapeless.::[com.github.jacoby6000.scoobie.ast.QueryProjection[shapeless.HNil],shapeless.HNil]],shapeless.::[com.github.jacoby6000.scoobie.ast.QueryUnion[shapeless.HNil],shapeless.::[com.github.jacoby6000.scoobie.ast.QueryUnion[shapeless.HNil],shapeless.HNil]],shapeless.::[Int,shapeless.HNil],shapeless.HNil,this.Out,shapeless.::[Int,shapeless.HNil],shapeless.::[Int,shapeless.HNil],this.Out] = QueryBuilder(QueryProjectOne(QueryPathEnd(bar),None),QueryProjectOne(QueryAdd(QueryPathEnd(foo),QueryParameter(10 :: HNil),10 :: HNil),Some(woozle)) :: QueryProjectAll :: HNil,QueryLeftOuterJoin(QueryProjectOne(QueryPathEnd(baz),S...

scala> val sql = q.build.genSql // Generate the sql associated with this query
sql: String = SELECT "foo" + ? AS woozle, * FROM "bar" LEFT OUTER JOIN "baz" AS b ON "bar"."id" = "b"."barId" INNER JOIN "biz" AS c ON "c"."id" = "bar"."bizId" WHERE "c"."name" = ?  AND  "c"."age" > ? ORDER BY "c"."age" DESC GROUP BY "b"."worth" ASC
```

The formatted output of this is

```sql
SELECT
    "foo" + ? AS woozle,
    
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

```scala
scala> def biggerThan(n: Int) = {
     |   (baseQuery where p"population" > n)
     |     .build
     |     .queryAndPrint[Country](sql => println("\n" + sql))
     | }
biggerThan: (n: Int)doobie.imports.Query0[Country]

scala> biggerThan(150000000).quick.unsafePerformSync

SELECT "code", "name", "population", "gnp" FROM "country"  WHERE "population" > ?
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
     |     .queryAndPrint[Country](sql => println("\n" + sql))
     | } 
populationIn: (r: Range)doobie.imports.Query0[Country]

scala> populationIn(150000000 to 200000000).quick.unsafePerformSync

SELECT "code", "name", "population", "gnp" FROM "country"  WHERE "population" >= ?  AND  "population" <= ?
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
     |     p"country" as "c2" on (
     |       func"reverse"(p"c1.code") === p"c2.code"
     |     )
     |   ) where (
     |     (p"c2.code" !== `null`) and
     |     (p"c2.name" !== p"c1.name")
     |   )).build
     |     .queryAndPrint[ComplimentaryCountries](sql => println("\n" + sql))
     | }
joined: doobie.imports.Query0[ComplimentaryCountries]

scala> joined.quick.unsafePerformSync

SELECT "c1"."code", "c1"."name", "c2"."code", "c2"."name" FROM "country" AS c1 LEFT OUTER JOIN "country" AS c2 ON "reverse"("c1"."code") = "c2"."code" WHERE "c2"."code" IS NOT NULL  AND  "c2"."name" <> "c1"."name"
  ComplimentaryCountries(PSE,Palestine,ESP,Spain)
  ComplimentaryCountries(YUG,Yugoslavia,GUY,Guyana)
  ComplimentaryCountries(ESP,Spain,PSE,Palestine)
  ComplimentaryCountries(SUR,Suriname,RUS,Russian Federation)
  ComplimentaryCountries(RUS,Russian Federation,SUR,Suriname)
  ComplimentaryCountries(VUT,Vanuatu,TUV,Tuvalu)
  ComplimentaryCountries(TUV,Tuvalu,VUT,Vanuatu)
  ComplimentaryCountries(GUY,Guyana,YUG,Yugoslavia)
```

Check out [this end to end example](https://github.com/Jacoby6000/scoobie/blob/master/core/src/test/scala/com/github/jacoby6000/scoobie/dsl/weak/SqlDSLSimpleSelectTest.scala#L71) for an idea of how to utilize insert/update/delete as well.
