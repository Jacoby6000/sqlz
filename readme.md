[![Maven Central](https://img.shields.io/maven-central/v/com.github.jacoby6000/scoobie-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jacoby6000/scoobie-core_2.12)
[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 
[![Build Status](https://travis-ci.org/Jacoby6000/scoobie.svg?branch=master)](https://travis-ci.org/Jacoby6000/scoobie) 
[![codecov](https://codecov.io/gh/Jacoby6000/scoobie/branch/master/graph/badge.svg)](https://codecov.io/gh/Jacoby6000/scoobie)

## Scala Development paused while I wait for a fix for [scala/scala#5744](https://github.com/scala/scala/pull/5744)

Issue 5744 explains that scala does not work properly with GADTs. After talking a lot with Edmund Noble and Greg Pfeil, I've discovered that to have the AST be reusable and arbitrarily extensible, I need to be able to use GADTs. 

You can see the work I've done towards that [here](https://github.com/Jacoby6000/scoobie/tree/feature/%2340-adjust-ast-to-support-fixpoint). 

### Dotty

I've also started working with dotty in an attempt to see if it will work in dotty.   Interestingly enough, most things do work, but whenever I try to invoke the algebra to execute a paramorphism, the compiler crashes. You can see the work done towards that, [here](https://github.com/Jacoby6000/scoobie/tree/dotty).

### Querying with [Doobie](https://github.com/tpolecat/doobie), without raw sql

The goal of this project is to produce an alternative to writing SQL queries for use with Doobie.

As it stands now, there is a quick 'n dirty SQL DSL, implemented with a lightweight AST. Other DSLs may be created in the future.

Check out the [documentation](https://jacoby6000.github.io/scoobie) for more information.
