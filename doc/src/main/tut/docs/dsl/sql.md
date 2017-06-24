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
val simpleSelect = select(p"foo", p"baz", 42 as "fortyTwo") from p"bar"
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
val bad = select("foo") from p"bar"
val good = select(p"foo") from p"bar"
```

The `bad` query will always just return the string "foo", while the `good` query will return the values that the column `foo` contains.

### Where

Where clauses are constructed using familiar scala comparisons:

```tut:book
val gt = p"x" > 30
val lt = p"x" < p"y"
val and = gt and lt
val or = gt or lt
val eq = p"a" === false
val not = not(p"a" >= 10)
```

### Joins

Using the DSL, you create join clauses just as you would in sql (albeit with a bit more syntax)

```tut:book
select(p"a.foo", p"b.bar") from (p"table1" as "a") leftOuterJoin ((p"table2" as "b") on (p"a.id" === p"b.a_id")).build
```

