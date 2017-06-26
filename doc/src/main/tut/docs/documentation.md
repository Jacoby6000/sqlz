---
title: Documentation
layout: docs
section: docs
position: 1
---

### Purpose

Scoobie at it's core is simply an AST which represents ANSI SQL.

As it exists today, there is support for interpreting the AST with doobie queries, and for creating AST nodes with a SQL-Like DSL.

To use Scoobie, pick the appropriate doobie version and database driver. Scalaz and shapeless versions are inherited from doobie.

To get going quickly, checkout the [Quickstart](./quickstart.html) page.

### Packages

| scoobie distribution              | scoobie version | doobie | status | jdk  | scala          | scalaz | scalaz-stream  | shapeless |
|:---------------------------------:|:---------------:|:------:|:------:|:----:|:--------------:|:------:|:--------------:|:---------:|
| scoobie-contrib-doobie40-postgres | 0.3.0           |  0.4.0 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie41-postgres | 0.3.0           |  0.4.1 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie40-mysql    | 0.3.0           |  0.4.0 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-doobie41-mysql    | 0.3.0           |  0.4.1 | stable | 1.8+ | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |
| scoobie-contrib-mild-sql-dsl      | 0.3.0           |  N/A   | stable | 1.6+ | 2.11.8/2.12.1  |   N/A  |      N/A       |    N/A    |

### Other Packages

| scoobie distribution              | scoobie version | jdk   | scala          | description                                                                                                                                                    |
|:---------------------------------:|:---------------:|:-----:|:--------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| scoobie-core                      | 0.3.0           |  1.6+ | 2.11.8/2.12.1  | Pulls in the core AST. Use this dependency if you are trying to create additional interpreters or DSLs                                                         |
| scoobie-contrib-ansi-sql          | 0.3.0           |  1.6+ | 2.11.8/2.12.1  | Pulls in the core AST plus an interpreter for interpreting ANSI SQL. Use this if you're looking to create an additional database driver backed by ANSI SQL     |



### Snapshots

| scoobie distribution              | scoobie version | doobie | scala          | scalaz | scalaz-stream  | shapeless | issues-resolved        |
|:---------------------------------:|:---------------:|:------:|:--------------:|:------:|:--------------:|:---------:|:-----------------------|
| scoobie-contrib-doobie40-postgres | 0.3.1-SNAPSHOT  |  0.4.0 | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |  [#51], [#52], [#55]   |
| scoobie-contrib-doobie41-postgres | 0.3.1-SNAPSHOT  |  0.4.1 | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |  [#51], [#52], [#55]   |
| scoobie-contrib-doobie40-mysql    | 0.3.1-SNAPSHOT  |  0.4.0 | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |  [#51], [#52], [#55]   |
| scoobie-contrib-doobie41-mysql    | 0.3.1-SNAPSHOT  |  0.4.1 | 2.11.8/2.12.1  |   7.2  |      0.8a      |    2.3    |  [#51], [#52], [#55]   |
| scoobie-contrib-mild-sql-dsl      | 0.3.1-SNAPSHOT  |  N/A   | 2.11.8/2.12.1  |   N/A  |      N/A       |    N/A    |  [#51], [#52], [#55]   |
| scoobie-contrib-ansi-sql          | 0.3.1-SNAPSHOT  |  N/A   | 2.11.8/2.12.1  |   7.2  |      N/A       |    N/A    |  [#51], [#52], [#55]   |
| scoobie-core                      | 0.3.1-SNAPSHOT  |  N/A   | 2.11.8/2.12.1  |   N/A  |      N/A       |    N/A    |  [#51], [#52], [#55]   |

[#51]: https://github.com/Jacoby6000/scoobie/issues/51
[#52]: https://github.com/Jacoby6000/scoobie/pull/52
[#55]: https://github.com/Jacoby6000/scoobie/issues/55

