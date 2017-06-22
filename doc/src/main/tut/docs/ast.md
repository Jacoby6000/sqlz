---
title: AST
layout: docs
section: docs
---

### The Scoobie AST

The Scoobie AST is a very simple construction of nodes which eventually make up a sql query. If you're looking to build DSLs or just want to understand the AST better, then keep reading.

#### Index

To get around quickly, use the index below.

* <code><a href="#query-expression">QueryExpression</a></code>
  * <code><a href="#query-select">QuerySelect</a></code>
  * <code><a href="#query-modify">QueryModify</a></code>
    * <code><a href="#query-insert">QueryInsert</a></code>
    * <code><a href="#query-update">QueryUpdate</a></code>
    * <code><a href="#query-delete">QueryDelete</a></code>
* <code><a href="#query-value">QueryValue</a></code>
  * <code><a href="#query-select">QuerySelect</a></code>
  * <code><a href="#query-parameter">QueryParameter</a></code>
  * <code><a href="#query-null">QueryNull</a></code>
  * <code><a href="#query-raw-expression">QueryRawExpression</a></code>
  * <code><a href="#query-function">QueryFunction</a></code>
  * <code><a href="#query-arithmetic">QueryArithmetic</a></code>
    * <code><a href="#query-add">QueryAdd</a></code>
    * <code><a href="#query-sub">QuerySub</a></code>
    * <code><a href="#query-div">QueryDiv</a></code>
    * <code><a href="#query-div">QueryMul</a></code>
  * <code><a href="#query-path">QueryPath</a></code>
    * <code><a href="#query-path-cons">QueryPathCons</a></code>
    * <code><a href="#query-path-end">QueryPathEnd</a></code>
* <code><a href="#query-comparison">QueryComparison</a></code>
  * <code><a href="#query-comparison-no-op">QueryComparisonNop</a></code>
  * <code><a href="#query-equal">QueryEqual</a></code>
  * <code><a href="#query-greater-than">QueryGreaterThan</a></code> 
  * <code><a href="#query-greater-than-or-equal">QueryGreaterThanOrEqual</a></code>
  * <code><a href="#query-less-than">QueryLessThan</a></code>
  * <code><a href="#query-less-than-or-equal">QueryLessThanOrEqual</a></code>
  * <code><a href="#query-and">QueryAnd</a></code>
  * <code><a href="#query-or">QueryOr</a></code>
  * <code><a href="#query-in">QueryIn</a></code>
  * <code><a href="#query-lit">QueryLit</a></code>
  * <code><a href="#query-not">QueryNot</a></code>
* <code><a href="#query-projection">QueryProjection</a></code>
  * <code><a href="#query-project-one">QueryProjectOne</a></code>
  * <code><a href="#query-project-all">QueryProjectAll</a></code>
* <code><a href="#query-join">QueryJoin</a></code>
  * <code><a href="#query-inner-join">QueryInnerJoin</a></code>
  * <code><a href="#query-full-outer-join">QueryFullOuterJoin</a></code>
  * <code><a href="#query-left-outer-join">QueryLeftOuterJoin</a></code>
  * <code><a href="#query-right-outer-join">QueryRightOuterJoin</a></code>
  * <code><a href="#query-cross-join">QueryCrossJoin</a></code>
* <code><a href="#query-sort">QuerySort</a></code>
  * <code><a href="#query-sort-asc">QuerySortAsc</a></code>
  * <code><a href="#query-sort-desc">QuerySortDesc</a></code>

### Query Expression

The highest level node that exists in the Scoobie AST is <code><a href="#query-expression">QueryExpression</a></code>. Where ever you have a <code><a href="#query-expression">QueryExpression</a></code>, you have a complete and executable query. 
At the same level as <code><a href="#query-expression">QueryExpression</a></code> is <code><a href="#query-modify">QueryModify</a></code> and <code><a href="#query-select">QuerySelect</a></code>. 

* <code><a href="#query-select">QuerySelect</a></code> exclusively represents Select statements.
* <code><a href="#query-modify">QueryModify</a></code> represents Inserts (<code><a href="#query-insert">QueryInsert</a></code>), Updates (<code><a href="#query-update">QueryUpdate</a></code>), and Deletes (<code><a href="#query-delete">QueryDelete</a></code>).

#### Query Select

A <code><a href="#query-select">QuerySelect</a></code> is comprised of 8 parts.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the table that is being selected from. 
2. a <code>List[<a href="#query-projection">QueryProjection</a>]</code> of all the datums to be selected.
3. a <code>List[<a href="#query-join">QueryJoin</a>]</code> of all the joins to be performed.
4. a <code><a href="#query-comparison">QueryComparison</a></code> representing the where/filter clause of a query.
5. a <code>List[<a href="#query-sort">QuerySort</a>]</code> representing the sort actions.
6. a <code>List[<a href="#query-sort">QuerySort</a>]</code> representing the groupBy actions.
7. a `Option[Long]` representing the query offset.
8. a `Option[Long]` representing the limit.

All <code><a href="#query-select">QuerySelect</a></code> values extend <code><a href="#query-expression">QueryExpression</a></code> and <code><a href="#query-value">QueryValue</a></code>. <code><a href="#query-select">QuerySelect</a></code> extending <code><a href="#query-value">QueryValue</a></code> has great significance regarding subqueries.

#### Query Update

A <code><a href="#query-update">QueryUpdate</a></code> is comprised of 3 parts.
1. a <code><a href="#query-path">QueryPath</a></code> representing the collection to perform the update on.
2. a `List[ModifyField]` representing the fields to be modified and their associated new values.
3. a <code><a href="#query-comparison">QueryComparison</a></code> representing the where/filter clause of the update.

All <code><a href="#query-update">QueryUpdate</a></code> values extend <code><a href="#query-expression">QueryExpression</a></code> and <code><a href="#query-modify">QueryModify</a></code>.

#### Query Insert

A <code><a href="#query-insert">QueryInsert</a></code> is comprised of 2 parts. 
1. a <code><a href="#query-path">QueryPath</a></code> representing the collection to perform the insert on.
2. a `List[ModifyField]` representing the values of each column.

All <code><a href="#query-insert">QueryInsert</a></code> values extend <code><a href="#query-expression">QueryExpression</a></code> and <code><a href="#query-modify">QueryModify</a></code>.

#### Query Delete

A <code><a href="#query-delete">QueryDelete</a></code> is comprised of 2 parts.
1. a <code><a href="#query-path">QueryPath</a></code> representing the collection to perform the delete on.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the where/filter clause of the delete.

All <code><a href="#query-delete">QueryDelete</a></code> values extends <code><a href="#query-expression">QueryExpression</a></code> and <code><a href="#query-modify">QueryModify</a></code>.

#### Modify Field

A `ModifyField` value is comporised of two parts.
1. a <code><a href="#query-path">QueryPath</a></code> representing the column to be modified.
2. a <code><a href="#query-value">QueryValue</a></code> representing the value.

`ModifyField` has no sub or super-classes. It's a very specific use case.  It is essentially a specialized tuple which one might view as
```scala
type ModifyField = (<a href="#query-path">QueryPath</a>, <a href="#query-value">QueryValue</a>)
```

It exists only to pair a path with a value. 

### Query Value

A <code><a href="#query-value">QueryValue</a></code> is the super-type for anything that can be used in a SQL ast in a value position. This means much of the AST extends it. 

Members of <a href="#query-value">QueryValue</a> include:
* <code><a href="#query-parameter">QueryParameter</a></code>
* <code><a href="#query-null">QueryNull</a></code>
* <code><a href="#query-raw-expression">QueryRawExpression</a></code>
* <code><a href="#query-function">QueryFunction</a></code>
* <code><a href="#query-add">QueryAdd</a></code>
* <code><a href="#query-sub">QuerySub</a></code>
* <code><a href="#query-div">QueryDiv</a></code>
* <code><a href="#query-div">QueryMul</a></code>
* <code><a href="#query-select">QuerySelect</a></code>
* <code><a href="#query-path">QueryPath</a></code>

#### Query Raw Expression

A <code><a href="#query-raw-expression">QueryRawExpression</a></code> simply represents a value who should not be evaluated in to a prepared statement. These are typically dangerous, but useful if you need to insert some custom sql that the AST does not support.

A <code><a href="#query-raw-expression">QueryRawExpression</a></code> is comprised of two elements.
1. Some type `T` which represents the value to be raw-ly interpreted
2. Some evidence `RawExpressionHandler[T]` which represents the method for which to express `T` as a query string.

#### Query Parameter

A <code><a href="#query-parameter">QueryParameter</a></code> represents any value that can be used in a prepared statement. For example with scoobie, this can be any type `T` for which there exists a `Meta[T]` instance.

All of the `F[_]` schenanigans that have gone unmentioned thus far stem from <code><a href="#query-parameter">QueryParameter</a></code>. Since <code><a href="#query-parameter">QueryParameter</a></code> is generic and the AST knows nothing about type `T`, we must be provided a method for converting `T` in to useful values. That's where `F[T]` comes in.

A <code><a href="#query-parameter">QueryParameter</a></code> is comprised of two elements.
1. Some type `T` which represents the value.
2. Some evidence `F[T]` which will later be used in an interpreter to generate useful interpretations of `T`. In the context of Scoobie with Doobie, `F` would represent a `ScoobieFragmentProducer`. Scoobie can produce a `ScoobieFragmentProducer` for any `T` which has an implicit `Meta[T]` in scope.

#### Query Function

A <code><a href="#query-function">QueryFunction</a></code> represents a sql function to be run. 

A <code><a href="#query-function">QueryFunction</a></code> has two members.
1. a <code><a href="#query-path">QueryPath</a></code> which represents the function to be called.
2. a <code>List[<a href="#query-value">QueryValue</a>]</code> representing each parameter to be passed in to the given function.

<code><a href="#query-function">QueryFunction</a></code> is unsafe if you are not careful, because Scoobie doesn't know how many parameters a function should accept.  
When creating a DSL it's fine to expose <a href="#query-function">QueryFunction</a> with varargs, but try to create specializations so that users are less likely to generate invalid queries.

#### Query Null

A <code><a href="#query-null">QueryNull</a></code> represents a SQL null.

### Query Arithmetic

All Arithmetic instances extend <code><a href="#query-value">QueryValue</a></code>, and accept <code><a href="#query-value">QueryValue</a></code>s as input. This means that all <code><a href="#query-arithmetic">QueryArithmetic</a></code> composes.

There are currently 4 members in Query Arithmetic.
* <code><a href="#query-add">QueryAdd</a></code>
* <code><a href="#query-sub">QuerySub</a></code>
* <code><a href="#query-div">QueryDiv</a></code>
* <code><a href="#query-div">QueryMul</a></code>

#### Query Add

A <code><a href="#query-add">QueryAdd</a></code> represents a simple addition operation.

A <code><a href="#query-add">QueryAdd</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> which represents the left side of the addition.
2. a <code><a href="#query-value">QueryValue</a></code> which represents the right side of the addition.

#### Query Subtract

A <code><a href="#query-sub">QuerySub</a></code> represents a simple addition operation.

A <code><a href="#query-sub">QuerySub</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> which represents the left side of the addition.
2. a <code><a href="#query-value">QueryValue</a></code> which represents the right side of the addition.

#### Query Divide

A <code><a href="#query-div">QueryDiv</a></code> represents a simple addition operation.

A <code><a href="#query-div">QueryDiv</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> which represents the left side of the addition.
2. a <code><a href="#query-value">QueryValue</a></code> which represents the right side of the addition.

#### Query Multiply

A <code><a href="#query-div">QueryMul</a></code> represents a simple addition operation.

A <code><a href="#query-div">QueryMul</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> which represents the left side of the addition.
2. a <code><a href="#query-value">QueryValue</a></code> which represents the right side of the addition.

### Query Path

A <code><a href="#query-path">QueryPath</a></code> represents a path to a location in the database. All <code><a href="#query-path">QueryPath</a></code>s extend <code><a href="#query-value">QueryValue</a></code>

<code><a href="#query-path">QueryPath</a></code> has two sub-classes.
* <code><a href="#query-path-cons">QueryPathCons</a></code>
* <code><a href="#query-path-end">QueryPathEnd</a></code>

A query path like `foo.bar` may be represented by <code><a href="#query-path-cons">QueryPathCons</a>("foo", <a href="#query-path-end">QueryPathEnd</a>("bar"))</code>. A good way to think of a <code><a href="#query-path">QueryPath</a></code> in the context of the Scoobie AST is as a type of `Cons` list. <code><a href="#query-path-end">QueryPathEnd</a></code> represents `Nil`, and <code><a href="#query-path-cons">QueryPathCons</a></code> represents a piece that is not yet the end.

#### Query Path Cons

A <code><a href="#query-path-cons">QueryPathCons</a></code> represents any part of the path that is not the end.

A <code><a href="#query-path-cons">QueryPathCons</a></code> has two members.
1. a `String` representing this portion of the path.
2. a <code><a href="#query-path">QueryPath</a></code> representing the next part of the path.

#### Query Path End

A <code><a href="#query-path-end">QueryPathEnd</a></code> simply represents the end of a path.

A <code><a href="#query-path-end">QueryPathEnd</a></code> has a single member.
1. a `String` representing the path's final piece.

### Query Projection

A <code><a href="#query-projection">QueryProjection</a></code> represents a projected piece of data. This could be anything that is aliased, like a column name, or a derived query.

<code><a href="#query-projection">QueryProjection</a></code> has two sub-classes
* <code><a href="#query-project-one">QueryProjectOne</a></code>
* <code><a href="#query-project-all">QueryProjectAll</a></code>

#### Query Project All

<code><a href="#query-project-all">QueryProjectAll</a></code> simply represents `*` in a select statement. 

#### Query Project One

<code><a href="#query-project-one">QueryProjectOne</a></code> can represent a column, a derived query, or a function result.

<code><a href="#query-project-one">QueryProjectOne</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the selection to be projected
2. an `Option[String]` representing the alias for the projection. Note that the alias is not required.

### Query Comparison

A <code><a href="#query-comparison">QueryComparison</a></code> is a node representing a where/filter clause in a SQL query.

<code><a href="#query-comparison">QueryComparison</a></code> has 11 sub-classes.
* <code><a href="#query-comparison-no-op">QueryComparisonNop</a></code>
* <code><a href="#query-equal">QueryEqual</a></code>
* <code><a href="#query-greater-than">QueryGreaterThan</a></code>
* <code><a href="#query-greater-than-or-equal">QueryGreaterThanOrEqual</a></code>
* <code><a href="#query-less-than">QueryLessThan</a></code>
* <code><a href="#query-less-than-or-equal">QueryLessThanOrEqual</a></code>
* <code><a href="#query-and">QueryAnd</a></code>[^1]
* <code><a href="#query-or">QueryOr</a></code>[^1]
* <code><a href="#query-in">QueryIn</a></code>
* <code><a href="#query-lit">QueryLit</a></code>
* <code><a href="#query-not">QueryNot</a></code>[^1]

[^1]: Is composable with other comparisons.

#### Query Comparison NoOp

A <code><a href="#query-comparison-no-op">QueryComparisonNop</a></code> represents a NoOp. It does nothing. For example
* <code><a href="#query-and">QueryAnd</a>(<a href="#query-comparison-no-op">QueryComparisonNop</a>, foo) === foo</code>
* <code><a href="#query-or">QueryOr</a>(<a href="#query-comparison-no-op">QueryComparisonNop</a>, foo) === foo</code>
* <code><a href="#query-not">QueryNot</a>(<a href="#query-comparison-no-op">QueryComparisonNop</a>) === <a href="#query-comparison-no-op">QueryComparisonNop</a></code>

#### Query Equal

A <code><a href="#query-equal">QueryEqual</a></code> instance compares two query values, asserting that they must be eqivelant.

<code><a href="#query-equal">QueryEqual</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code><a href="#query-value">QueryValue</a></code> representing the right side of the comparison.

#### Query Greater Than

A <code><a href="#query-greater-than">QueryGreaterThan</a></code> instance compares two query values, asserting that the left side must be greater than the right side.

<code><a href="#query-greater-than">QueryGreaterThan</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code><a href="#query-value">QueryValue</a></code> representing the right side of the comparison.

#### Query Greater Than Or Equal

A <code><a href="#query-greater-than-or-equal">QueryGreaterThanOrEqual</a></code> instance compares two query values, asserting that the left side must be greater than or equal to the right side.

<code><a href="#query-greater-than-or-equal">QueryGreaterThanOrEqual</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code><a href="#query-value">QueryValue</a></code> representing the right side of the comparison.

#### Query Less Than

A <code><a href="#query-less-than">QueryLessThan</a></code> instance compares two query values, asserting that the left side must be less than the right side.

<code><a href="#query-less-than">QueryLessThan</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code><a href="#query-value">QueryValue</a></code> representing the right side of the comparison.

#### Query Less Than Or Equal

A <code><a href="#query-less-than-or-equal">QueryLessThanOrEqual</a></code> instance compares two query values, asserting that the left side must be less than or equal to the right side.

<code><a href="#query-less-than-or-equal">QueryLessThanOrEqual</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code><a href="#query-value">QueryValue</a></code> representing the right side of the comparison.

#### Query In

A <code><a href="#query-in">QueryIn</a></code> checks to see if the left side value is in the right side collection

<code><a href="#query-in">QueryIn</a></code> has two members.
1. a <code><a href="#query-value">QueryValue</a></code> representing the left side of the comparison.
2. a <code>List[<a href="#query-value">QueryValue</a>]</code> representing the collection of things to compare the left side to.

#### Query And

A <code><a href="#query-and">QueryAnd</a></code> instance asserts that the left and right side comparisons must both be true.

<code><a href="#query-and">QueryAnd</a></code> has two members.
1. a <code><a href="#query-comparison">QueryComparison</a></code> representing the left comparison.
1. a <code><a href="#query-comparison">QueryComparison</a></code> representing the right comparison.

#### Query Or

A <code><a href="#query-or">QueryOr</a></code> instance asserts that the either the left or right side comparison must be true.

<code><a href="#query-or">QueryOr</a></code> has two members.
1. a <code><a href="#query-comparison">QueryComparison</a></code> representing the left comparison.
1. a <code><a href="#query-comparison">QueryComparison</a></code> representing the right comparison.

#### Query Literal

A <code><a href="#query-lit">QueryLit</a></code> simply promotes a <code><a href="#query-value">QueryValue</a></code> to the context of a <code><a href="#query-comparison">QueryComparison</a></code>. This is useful for things like boolean values, or bool-ish support that different SQL backends might have.

<code><a href="#query-lit">QueryLit</a></code> has a single member.
1. a <code><a href="#query-value">QueryValue</a></code> to be promoted to <code><a href="#query-comparison">QueryComparison</a></code> as <code><a href="#query-lit">QueryLit</a></code>

#### Query Not

A <code><a href="#query-not">QueryNot</a></code> inverts the result of the inner comparison.

<code><a href="#query-not">QueryNot</a></code> has a single member.
1. a <code><a href="#query-comparison">QueryComparison</a></code> whose result should be inverted.

### Query Join

A <code><a href="#query-join">QueryJoin</a></code> represents any of the join operations for a database.

<code><a href="#query-join">QueryJoin</a></code> has 5 sub-classes.
* <code><a href="#query-inner-join">QueryInnerJoin</a></code>
* <code><a href="#query-full-outer-join">QueryFullOuterJoin</a></code>
* <code><a href="#query-left-outer-join">QueryLeftOuterJoin</a></code>
* <code><a href="#query-right-outer-join">QueryRightOuterJoin</a></code>
* <code><a href="#query-cross-join">QueryCrossJoin</a></code>

#### Query Inner Join

A <code><a href="#query-inner-join">QueryInnerJoin</a></code> represents an inner join action on a database.

<code><a href="#query-inner-join">QueryInnerJoin</a></code> has two members.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the dataset being joined with.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the condition to join with.

#### Query Full Outer Join

A <code><a href="#query-full-outer-join">QueryFullOuterJoin</a></code> represents an outer join action on a database.

<code><a href="#query-full-outer-join">QueryFullOuterJoin</a></code> has two members.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the dataset being joined with.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the condition to join with.

#### Query Left Outer Join

A <code><a href="#query-left-outer-join">QueryLeftOuterJoin</a></code> represents a left outer join action on a database.

<code><a href="#query-left-outer-join">QueryLeftOuterJoin</a></code> has two members.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the dataset being joined with.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the condition to join with.

#### Query Right Outer Join

A <code><a href="#query-right-outer-join">QueryRightOuterJoin</a></code> represents a right outer join action on a database.

<code><a href="#query-right-outer-join">QueryRightOuterJoin</a></code> has two members.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the dataset being joined with.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the condition to join with.

#### Query Cross Join

A <code><a href="#query-cross-join">QueryCrossJoin</a></code> represents a cross join action on a database.

<code><a href="#query-cross-join">QueryCrossJoin</a></code> has two members.
1. a <code><a href="#query-projection">QueryProjection</a></code> representing the dataset being joined with.
2. a <code><a href="#query-comparison">QueryComparison</a></code> representing the condition to join with.

### Query Sort

A <code><a href="#query-sort">QuerySort</a></code> represents the sort method to use on some database column.

<code><a href="#query-sort">QuerySort</a></code> has two sub-classes.
* <code><a href="#query-sort-asc">QuerySortAsc</a></code>
* <code><a href="#query-sort-desc">QuerySortDesc</a></code>

#### Query Sort Ascending

<code><a href="#query-sort-asc">QuerySortAsc</a></code> represents an ascending sort.

<code><a href="#query-sort-asc">QuerySortAsc</a></code> has one member.
1. a <code><a href="#query-path">QueryPath</a></code> representing the column to sort by.

#### Query Sort Descending

<code><a href="#query-sort-desc">QuerySortDesc</a></code> represents a descending sort.

<code><a href="#query-sort-desc">QuerySortDesc</a></code> has one member.
1. a <code><a href="#query-path">QueryPath</a></code> representing the column to sort by.
