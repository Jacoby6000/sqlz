---
title: AST
layout: docs
section: docs
---

### The Scoobie AST

The Scoobie AST is a very simple construction of nodes which eventually make up a sql query. If you're looking to build DSLs or just want to understand the AST better, then keep reading.

#### Index

To get around quickly, use the index below.

* <code><a href="#query-expression">QueryExpression(#query-expression)</a></code>
  * `QuerySelect`
  * `QueryModify`
    * `QueryInsert`
    * `QueryUpdate`
    * `QueryDelete`
* `QueryValue`
  * `QuerySelect`
  * `QueryParameter`
  * `QueryNull`
  * `QueryRawExpression`
  * `QueryFunction`
  * `QueryArithmetic`
    * `QueryAdd`
    * `QuerySub`
    * `QueryDiv`
    * `QueryMul`
  * `QueryPath`
    * `QueryPathCons`
    * `QueryPathEnd`
* `QueryComparison`
  * `QueryComparisonNop`
  * `QueryEqual`
  * `QueryGreaterThan` 
  * `QueryGreaterThanOrEqual`
  * `QueryLessThan`
  * `QueryLessThanOrEqual`
  * `QueryAnd`
  * `QueryOr`
  * `QueryIn`
  * `QueryLit`
  * `QueryNot`
* `QueryProjection`
  * `QueryProjectOne`
  * `QueryProjectAll`
* `QueryJoin`
  * `QueryInnerJoin`
  * `QueryFullOuterJoin`
  * `QueryLeftOuterJoin`
  * `QueryRightOuterJoin`
  * `QueryCrossJoin`
* `QuerySort`
  * `QuerySortAsc`
  * `QuerySortDesc`

### Query Expression

The highest level node that exists in the Scoobie AST is [`QueryExpression`](#query-expression). Where ever you have a [`QueryExpression`](#query-expression), you have a complete and executable query. 
At the same level as [`QueryExpression`](#query-expression) is `QueryModify` and `QuerySelect`. 

* `QuerySelect` exclusively represents Select statements.
* `QueryModify` represents Inserts (`QueryInsert`), Updates (`QueryUpdate`), and Deletes (`QueryDelete`).

#### Query Select

A `QuerySelect` is comprised of 8 parts.
1. a `QueryProjection` representing the table that is being selected from. 
2. a `List[QueryProjection]` of all the datums to be selected.
3. a `List[QueryJoin]` of all the joins to be performed.
4. a `QueryComparison` representing the where/filter clause of a query.
5. a `List[QuerySort]` representing the sort actions.
6. a `List[QuerySort]` representing the groupBy actions.
7. a `Option[Long]` representing the query offset.
8. a `Option[Long]` representing the limit.

All `QuerySelect` values extend [`QueryExpression`](#query-expression) and `QueryValue`. `QuerySelect` extending `QueryValue` has great significance regarding subqueries.

#### Query Update

A `QueryUpdate` is comprised of 3 parts.
1. a `QueryPath` representing the collection to perform the update on.
2. a `List[ModifyField]` representing the fields to be modified and their associated new values.
3. a `QueryComparison` representing the where/filter clause of the update.

All `QueryUpdate` values extend [`QueryExpression`](#query-expression) and `QueryModify`.

#### Query Insert

A `QueryInsert` is comprised of 2 parts. 
1. a `QueryPath` representing the collection to perform the insert on.
2. a `List[ModifyField]` representing the values of each column.

All `QueryInsert` values extend [`QueryExpression`](#query-expression) and `QueryModify`.

#### Query Delete

A `QueryDelete` is comprised of 2 parts.
1. a `QueryPath` representing the collection to perform the delete on.
2. a `QueryComparison` representing the where/filter clause of the delete.

All `QueryDelete` values extends [`QueryExpression`](#query-expression) and `QueryModify`.

#### Modify Field

A `ModifyField` value is comporised of two parts.
1. a `QueryPath` representing the column to be modified.
2. a `QueryValue` representing the value.

`ModifyField` has no sub or super-classes. It's a very specific use case.  It is essentially a specialized tuple which one might view as
```scala
type ModifyField = (QueryPath, QueryValue)
```

It exists only to pair a path with a value. 

### Query Value

A `QueryValue` is the super-type for anything that can be used in a SQL ast in a value position. This means much of the AST extends it. 

Members of QueryValue include:
* `QueryParameter`
* `QueryNull`
* `QueryRawExpression`
* `QueryFunction`
* `QueryAdd`
* `QuerySub`
* `QueryDiv`
* `QueryMul`
* `QuerySelect`
* `QueryPath`

#### Query Raw Expression

A `QueryRawExpression` simply represents a value who should not be evaluated in to a prepared statement. These are typically dangerous, but useful if you need to insert some custom sql that the AST does not support.

A `QueryRawExpression` is comprised of two elements.
1. Some type `T` which represents the value to be raw-ly interpreted
2. Some evidence `RawExpressionHandler[T]` which represents the method for which to express `T` as a query string.

#### Query Parameter

A `QueryParameter` represents any value that can be used in a prepared statement. For example with scoobie, this can be any type `T` for which there exists a `Meta[T]` instance.

All of the `F[_]` schenanigans that have gone unmentioned thus far stem from `QueryParameter`. Since `QueryParameter` is generic and the AST knows nothing about type `T`, we must be provided a method for converting `T` in to useful values. That's where `F[T]` comes in.

A `QueryParameter` is comprised of two elements.
1. Some type `T` which represents the value.
2. Some evidence `F[T]` which will later be used in an interpreter to generate useful interpretations of `T`. In the context of Scoobie with Doobie, `F` would represent a `ScoobieFragmentProducer`. Scoobie can produce a `ScoobieFragmentProducer` for any `T` which has an implicit `Meta[T]` in scope.

#### Query Function

A `QueryFunction` represents a sql function to be run. 

A `QueryFunction` has two members.
1. a `QueryPath` which represents the function to be called.
2. a `List[QueryValue]` representing each parameter to be passed in to the given function.

`QueryFunction` is unsafe if you are not careful, because Scoobie doesn't know how many parameters a function should accept.  
When creating a DSL it's fine to expose QueryFunction with varargs, but try to create specializations so that users are less likely to generate invalid queries.

#### Query Null

A `QueryNull` represents a SQL null.

### Query Arithmetic

All Arithmetic instances extend `QueryValue`, and accept `QueryValue`s as input. This means that all `QueryArithmetic` composes.

There are currently 4 members in Query Arithmetic.
* `QueryAdd`
* `QuerySub`
* `QueryDiv`
* `QueryMul`

#### Query Add

A `QueryAdd` represents a simple addition operation.

A `QueryAdd` has two members.
1. a `QueryValue` which represents the left side of the addition.
2. a `QueryValue` which represents the right side of the addition.

#### Query Subtract

A `QuerySub` represents a simple addition operation.

A `QuerySub` has two members.
1. a `QueryValue` which represents the left side of the addition.
2. a `QueryValue` which represents the right side of the addition.

#### Query Divide

A `QueryDiv` represents a simple addition operation.

A `QueryDiv` has two members.
1. a `QueryValue` which represents the left side of the addition.
2. a `QueryValue` which represents the right side of the addition.

#### Query Multiply

A `QueryMul` represents a simple addition operation.

A `QueryMul` has two members.
1. a `QueryValue` which represents the left side of the addition.
2. a `QueryValue` which represents the right side of the addition.

### Query Path

A `QueryPath` represents a path to a location in the database. All `QueryPath`s extend `QueryValue`

`QueryPath` has two sub-classes.
* `QueryPathCons`
* `QueryPathEnd`

A query path like `foo.bar` may be represented by QueryPathCons("foo", QueryPathEnd("bar")). A good way to think of a QueryPath in the context of the Scoobie AST is as a type of `Cons` list. `QueryPathEnd` represents `Nil`, and `QueryPathCons` represents a piece that is not yet the end.

#### Query Path Cons

A `QueryPathCons` represents any part of the path that is not the end.

A `QueryPathCons` has two members.
1. a `String` representing this portion of the path.
2. a `QueryPath` representing the next part of the path.

#### Query Path End

A `QueryPathEnd` simply represents the end of a path.

A `QueryPathEnd` has a single member.
1. a `String` representing the path's final piece.

### Query Projection

A `QueryProjection` represents a projected piece of data. This could be anything that is aliased, like a column name, or a derived query.

`QueryProjection` has two sub-classes
* `QueryProjectOne`
* `QueryProjectAll`

#### Query Project All

`QueryProjectAll` simply represents `*` in a select statement. 

#### Query Project One

`QueryProjectOne` can represent a column, a derived query, or a function result.

`QueryProjectOne` has two members.
1. a `QueryValue` representing the selection to be projected
2. an `Option[String]` representing the alias for the projection. Note that the alias is not required.

### Query Comparison

A `QueryComparison` is a node representing a where/filter clause in a SQL query.

`QueryComparison` has 11 sub-classes.
* `QueryComparisonNop`
* `QueryEqual`
* `QueryGreaterThan`
* `QueryGreaterThanOrEqual`
* `QueryLessThan`
* `QueryLessThanOrEqual`
* `QueryAnd`[^1]
* `QueryOr`[^1]
* `QueryIn`
* `QueryLit`
* `QueryNot`[^1]


[^1]: 
  Is composable with other comparisons.

#### Query Comparison NoOp

A `QueryComparisonNop` represents a NoOp. It does nothing. For example
* `QueryAnd(QueryComparisonNop, foo) === foo`
* `QueryOr(QueryComparisonNop, foo) === foo`
* `QueryNot(QueryComparisonNop) === QueryComparisonNop`

#### Query Equal

A `QueryEqual` instance compares two query values, asserting that they must be eqivelant.

`QueryEqual` has two members.
1. a `QueryValue` representing the left side of the comparison.
2. a `QueryValue` representing the right side of the comparison.

#### Query Greater Than

A `QueryGreaterThan` instance compares two query values, asserting that the left side must be greater than the right side.

`QueryGreaterThan` has two members.
1. a `QueryValue` representing the left side of the comparison.
2. a `QueryValue` representing the right side of the comparison.

#### Query Greater Than Or Equal

A `QueryGreaterThanOrEqual` instance compares two query values, asserting that the left side must be greater than or equal to the right side.

`QueryGreaterThanOrEqual` has two members.
1. a `QueryValue` representing the left side of the comparison.
2. a `QueryValue` representing the right side of the comparison.

#### Query Less Than

A `QueryLessThan` instance compares two query values, asserting that the left side must be less than the right side.

`QueryLessThan` has two members.
1. a `QueryValue` representing the left side of the comparison.
2. a `QueryValue` representing the right side of the comparison.

#### Query Less Than Or Equal

A `QueryLessThanOrEqual` instance compares two query values, asserting that the left side must be less than or equal to the right side.

`QueryLessThanOrEqual` has two members.
1. a `QueryValue` representing the left side of the comparison.
2. a `QueryValue` representing the right side of the comparison.

#### Query And

A `QueryAnd` instance asserts that the left and right side comparisons must both be true.

`QueryAnd` has two members.
1. a `QueryComparison` representing the left comparison.
1. a `QueryComparison` representing the right comparison.

#### Query Or

A `QueryOr` instance asserts that the either the left or right side comparison must be true.

`QueryOr` has two members.
1. a `QueryComparison` representing the left comparison.
1. a `QueryComparison` representing the right comparison.

#### Query Literal

A `QueryLit` simply promotes a `QueryValue` to the context of a `QueryComparison`. This is useful for things like boolean values, or bool-ish support that different SQL backends might have.

`QueryLit` has a single member.
1. a `QueryValue` to be promoted to `QueryComparison` as `QueryLit`

#### Query Not

A `QueryNot` inverts the result of the inner comparison.

`QueryNot` has a single member.
1. a `QueryComparison` whose result should be inverted.

### Query Join

A `QueryJoin` represents any of the join operations for a database.

`QueryJoin` has 5 sub-classes.
* `QueryInnerJoin`
* `QueryFullOuterJoin`
* `QueryLeftOuterJoin`
* `QueryRightOuterJoin`
* `QueryCrossJoin`

#### Query Inner Join

A `QueryInnerJoin` represents an inner join action on a database.

`QueryInnerJoin` has two members.
1. a `QueryProjection` representing the dataset being joined with.
2. a `QueryComparison` representing the condition to join with.

#### Query Full Outer Join

A `QueryFullOuterJoin` represents an outer join action on a database.

`QueryFullOuterJoin` has two members.
1. a `QueryProjection` representing the dataset being joined with.
2. a `QueryComparison` representing the condition to join with.

#### Query Left Outer Join

A `QueryLeftOuterJoin` represents a left outer join action on a database.

`QueryLeftOuterJoin` has two members.
1. a `QueryProjection` representing the dataset being joined with.
2. a `QueryComparison` representing the condition to join with.

#### Query Right Outer Join

A `QueryRightOuterJoin` represents a right outer join action on a database.

`QueryRightOuterJoin` has two members.
1. a `QueryProjection` representing the dataset being joined with.
2. a `QueryComparison` representing the condition to join with.

#### Query Cross Join

A `QueryCrossJoin` represents a cross join action on a database.

`QueryCrossJoin` has two members.
1. a `QueryProjection` representing the dataset being joined with.
2. a `QueryComparison` representing the condition to join with.

### Query Sort

A `QuerySort` represents the sort method to use on some database column.

`QuerySort` has two sub-classes.
* `QuerySortAsc`
* `QuerySortDesc`

#### Query Sort Ascending

`QuerySortAsc` represents an ascending sort.

`QuerySortAsc` has one member.
1. a `QueryPath` representing the column to sort by.

#### Query Sort Descending

`QuerySortDesc` represents a descending sort.

`QuerySortDesc` has one member.
1. a `QueryPath` representing the column to sort by.
