---
title: SQL DSL
layout: docs
section: docs
---

### The SQL DSL

The SQL dsl attempts to closely mimic standard SQL syntax. 

To get started with the DSL, import the scoobie DSL, and an interpreter.

```tut:silent
import scoobie.doobie.doo.postgres._
import scoobie.snacks.mild.sql._
```

From here, you can start throwing queries together. 

```tut:book
val simpleSelect = select(p"foo", p"baz") from p"bar"
```

The above expression wil generate a `QueryBuilder` for a simple `select` query. You can attach more to it with things like `where`, `offset`, `limit`, `orderBy`, `leftOuterJoin`, and more. Before we get there, lets talk about `p`.

### Paths

The `p` string interpolator will take a standard string, and convert it to a `QueryPath` for use in queries, seen below

```tut:book
val shortPath = p"foo"
val longPath = p"foo.bar"
val longerPath = p"foo.bar.baz"
```

The differences between using a `QueryPath` and a `String` in a query are pretty drastic. Consider these two queries

```tut:silent
val compareColumn = select(p"foo") from(p"bar") where p"foo" === p"baz"
val compareValue = select(p"foo") from(p"bar") where p"foo" === "baz"
```

The `compareColumn` query will compare columns `foo` and `baz`, while the `compareValue` query will compare the value in column `foo` to the string "baz".

### Where

Where clauses are constructed using mostly familiar scala comparisons:

```tut:book
val gt = p"x" > 30
val lt = p"x" < p"y"
val gte = p"x" >= 30
val lte = p"x" <= p"y"
val eq = p"a" === false
val invert = not(p"a" >= 10)
```

Start a where clause in a traditional `where` position, like during a join, select, update, or delete.

```tut:book
val selectWhere = select(p"foo") from(p"bar") where (p"bar.id" === 3)
val updateWhere = update(p"bar") set (p"foo" ==> "baz") where (p"bar.id" === 3)
val deleteWhere = deleteFrom(p"foo") where(p"bar.id" === 3)
val joinOn = select(p"foo") from(p"bar") leftOuterJoin((p"baz" as "baz") on (p"bar.id" === p"baz.bar_id"))
```

You can combine multiple comparisons using `and`/`or`.

```tut:book
val foo = p"x" > 30 and p"y" < 10
```


### Joins

Using the DSL, you create join clauses just as you would in sql (albeit with a bit more syntax)

```tut:book
select(p"a.foo", p"b.bar") from (p"table1" as "a") leftOuterJoin ((p"table2" as "b") on (p"a.id" === p"b.a_id"))
```

Valid joins include `leftOuterJoin, `rightOuterJoin`, `innerJoin`, `fullOuterJoin`, and `crossJoin`.


