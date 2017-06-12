/**
  * Large portions of this build are based on @tpolecat's (Rob Norris) build file for doobie. Any genius found here is courtesy of him.
  */

import UnidocKeys._
import ReleaseTransformations._
import ScoobieUtil._

lazy val scoobie =
  project.in(file("."))
    .settings(name := "scoobie")
    .settings(scoobieSettings ++ noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(docs))
    .settings(
      tutSourceDirectory := file("doc") / "src" / "main" / "tut",
      tutTargetDirectory := file("doc") / "target" / "scala-2.11" / "tut"
    )
    .dependsOn(core, doobiePostgres, weakSqlDsl)
    .aggregate(core, doobieSupport, doobieSupport40, doobieSupport41, doobiePostgres, doobiePostgres40, doobiePostgres41, weakSqlDsl, docs, ansiSql)

lazy val core =
  project.in(file("core"))
    .enablePlugins(SbtOsgi)
    .settings(name := "scoobie-core")
    .settings(description := "AST for making convenient SQL DSLs in Scala.")
    .settings(scoobieSettings ++ publishSettings)
    .settings(libraryDependencies ++= Seq(specs))
    .settings(packageInfoGenerator("scoobie", "scoobie-core"))

lazy val doobieCorePlugin = ScoobieUtil.doobiePlugin(
  doobieCore,
  "support",
  "Introduces doobie support to scoobie"
)

lazy val doobieCoreFile = doobieCorePlugin.dir
lazy val doobieCoreSettings = doobieCorePlugin.settings

lazy val doobieSupport =
  project.in(doobieCoreFile)
    .settings(doobieCoreSettings.head)
    .settings(noPublishSettings)
    .dependsOn(core)

lazy val doobieSupport41 =
  project.in(file("doobie-support"))
    .settings(doobieCoreSettings.tail.tail.head)
    .enablePlugins(SbtOsgi)
    .settings(publishSettings)
    .dependsOn(core)

lazy val doobieSupport40 =
  project.in(file("doobie-support"))
    .enablePlugins(SbtOsgi)
    .settings(doobieCoreSettings.tail.head)
    .settings(publishSettings)
    .dependsOn(core)

lazy val doobiePostgresPlugin = ScoobieUtil.doobiePlugin(
    doobiePGDriver,
    "postgres",
    "Introduces doobie support to scoobie with postgres."
  )

lazy val doobiePgFile = doobiePostgresPlugin.dir
lazy val doobiePgSettings = doobiePostgresPlugin.settings

lazy val doobiePostgres =
  project.in(doobiePgFile)
    .settings(noPublishSettings)
    .settings(doobiePgSettings.head)
    .dependsOn(doobieSupport, ansiSql, weakSqlDsl % "test")

lazy val doobiePostgres41 =
  project.in(file("doobie-postgres"))
    .enablePlugins(SbtOsgi)
    .settings(publishSettings)
    .settings(doobiePgSettings.tail.head)
    .dependsOn(doobieSupport41, ansiSql, weakSqlDsl % "test")

lazy val doobiePostgres40 =
  project.in(file("doobie-postgres"))
    .enablePlugins(SbtOsgi)
    .settings(publishSettings)
    .settings(doobiePgSettings.tail.tail.head)
    .dependsOn(doobieSupport40, ansiSql, weakSqlDsl % "test")

lazy val weakSqlDsl =
  project.in(file("mild-sql-dsl"))
    .enablePlugins(SbtOsgi)
    .settings(scoobieSettings ++ publishSettings)
    .settings(name := "scoobie-contrib-mild-sql-dsl")
    .settings(description := "Introduces a weakly typed SQL DSL to scoobie.")
    .settings(libraryDependencies += specs)
    .settings(packageInfoGenerator("scoobie.dsl.weaksql", "scoobie-contrib-mild-sql-dsl"))
    .dependsOn(core)

lazy val ansiSql =
  project.in(file("ansi-sql"))
    .enablePlugins(SbtOsgi)
    .settings(publishSettings)
    .settings(name := "scoobie-contrib-ansi-sql")
    .settings(description := "Provides an ANSI-SQL interpreter for use with the Scoobie AST.")
    .settings(libraryDependencies ++= Seq(scalaz, specs))
    .settings(packageInfoGenerator("scoobie.doobie.doo.ansi", "scoobie-ansi-sql"))
    .settings(scoobieSettings)
    .dependsOn(core, weakSqlDsl % "test")

lazy val docs =
  project.in(file("doc"))
    .enablePlugins(TutPlugin)
    .settings(scoobieSettings ++ noPublishSettings)
    .settings(
      ctut := {
        val src = crossTarget.value / "tut"
        val dst = file("../jacoby6000.github.io/_scoobie-" + version.value + "/")
        if (!src.isDirectory) {
          println("Input directory " + src + " not found.")
        } else if (!dst.isDirectory) {
          println("Output directory " + dst + " not found.")
        } else {
          println("Copying to " + dst.getPath)
          val map = src.listFiles.filter(_.getName.endsWith(".md")).map(f => (f, new File(dst, f.getName)))
          IO.copy(map, overwrite = true, preserveLastModified = false)
        }
      }
    )
    .dependsOn(doobiePostgres41, weakSqlDsl)
