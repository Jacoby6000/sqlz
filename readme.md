# Abandoned

I am no longer working on this.  Master is currently very broken, and probably does not have good architecture. It is a partial implementation of SQL using tagless algebras...

I am giving up, because the way to correctly do this would be with a mutually recursive AST.  I had implemented this in an old version of dotty (see the dotty branch) where it _appeared_ to work, but there were some issues when evaluating the recursive paramorphism. Also, I should've been using `histoM` instead of `paraM` looking back... Anyway, it doesn't matter. Scala doesn't support GADTs and what I had working with Dotty no longer compiles for reasons I don't understand (though, admittedly, I didn't try that hard). 

This should be correctly doable using tagless algebras as well, however I am not really a fan of that approach for this particular problem. Modelling SQL using a mutually recursive AST felt right... Tagless doesn't. That's just my opinion though.  Feel free to pick this up if you want.

------------------

[![Maven Central](https://img.shields.io/maven-central/v/com.github.jacoby6000/scoobie-core_2.12.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jacoby6000/scoobie-core_2.12)
[![Join the chat at https://gitter.im/Jacoby6000/Scala-SQL-AST](https://badges.gitter.im/Jacoby6000/Scala-SQL-AST.svg)](https://gitter.im/Jacoby6000/Scala-SQL-AST?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) 
[![Build Status](https://travis-ci.org/Jacoby6000/scoobie.svg?branch=master)](https://travis-ci.org/Jacoby6000/scoobie) 
[![codecov](https://codecov.io/gh/Jacoby6000/scoobie/branch/master/graph/badge.svg)](https://codecov.io/gh/Jacoby6000/scoobie)
