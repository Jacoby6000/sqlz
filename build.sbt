/**
  * Large portions of this build are based on @tpolecat's (Rob Norris) build file for doobie. Any genius found here is courtesy of him.
  */

import UnidocKeys._
import ReleaseTransformations._
import ScoobieUtil._

lazy val scoobie =
  project.in(file("."))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobie")
    .settings(scoobieSettings ++ noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(docs))
    .dependsOn(core, doobieSupport, doobiePostgres, doobieMySql, weakSqlDsl, ansiSql)
    .aggregate(core, doobieSupport, doobiePostgres, doobieMySql, weakSqlDsl, ansiSql)

lazy val scoobieDoobie40 =
  project.in(file("./.dummy/scoobieDoobie40"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobieDoobie40", target := (file(".") / "target40"))
    .settings(scoobieSettings ++ noPublishSettings)
    .aggregate(core, doobieSupport40, doobiePostgres40, doobieMySql40, weakSqlDsl)

lazy val scoobieDoobie41 =
  project.in(file("./.dummy/scoobieDoobie41"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobieDoobie41", target := (file(".") / "target41"))
    .settings(scoobieSettings ++ noPublishSettings)
    .aggregate(core, doobieSupport41, doobiePostgres41, doobieMySql41, weakSqlDsl)

lazy val core =
  project.in(file("core"))
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobie-core")
    .settings(description := "AST for making convenient SQL DSLs in Scala.")
    .settings(scoobieSettings ++ publishSettings)
    .settings(libraryDependencies ++= Seq(specs))
    .settings(packageInfoGenerator("scoobie", "scoobie-core"))

lazy val doobieCorePlugin = ScoobieUtil.doobiePlugin(
  Some(doobieCore),
  "support",
  "Introduces doobie support to scoobie"
)

lazy val doobieCoreFile = doobieCorePlugin.dir
lazy val doobieCoreSettings = doobieCorePlugin.settings

lazy val doobieSupport =
  project.in(doobieCoreFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(doobieCoreSettings.head)
    .settings(noPublishSettings)
    .dependsOn(core)

lazy val doobieSupport41 =
  project.in(doobieCoreFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(doobieCoreSettings.tail.head)
    .enablePlugins(SbtOsgi)
    .settings(publishSettings)
    .dependsOn(core)

lazy val doobieSupport40 =
  project.in(doobieCoreFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .enablePlugins(SbtOsgi)
    .settings(doobieCoreSettings.tail.tail.head)
    .settings(publishSettings)
    .dependsOn(core)

lazy val doobiePostgresPlugin = ScoobieUtil.doobiePlugin(
  Some(doobiePGDriver),
  "postgres",
  "Introduces doobie support to scoobie with postgres."
)

lazy val doobiePgFile = doobiePostgresPlugin.dir
lazy val doobiePgSettings = doobiePostgresPlugin.settings

lazy val doobiePostgres =
  project.in(doobiePgFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(noPublishSettings)
    .settings(doobiePgSettings.head)
    .dependsOn(doobieSupport % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val doobiePostgres41 =
  project.in(doobiePgFile)
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings)
    .settings(doobiePgSettings.tail.head)
    .dependsOn(doobieSupport41 % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val doobiePostgres40 =
  project.in(doobiePgFile)
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings)
    .settings(doobiePgSettings.tail.tail.head)
    .dependsOn(doobieSupport40 % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val doobieMySqlPlugin = ScoobieUtil.doobiePlugin(
  None,
  "mysql",
  "Introduces doobie support to scoobie with mysql"
)

lazy val doobieMySqlFile = doobieMySqlPlugin.dir
lazy val doobieMySqlSettings = doobieMySqlPlugin.settings.map(_ ++ Seq(libraryDependencies += ("mysql" % "mysql-connector-java" % "6.0.6")))

lazy val doobieMySql =
  project.in(doobieMySqlFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(noPublishSettings)
    .settings(doobieMySqlSettings.head)
    .dependsOn(doobieSupport % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val doobieMySql41 =
  project.in(doobieMySqlFile)
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings)
    .settings(doobieMySqlSettings.tail.head)
    .dependsOn(doobieSupport41 % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val doobieMySql40 =
  project.in(doobieMySqlFile)
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings)
    .settings(doobieMySqlSettings.tail.tail.head)
    .dependsOn(doobieSupport40 % "compile->compile;it->it;", ansiSql, weakSqlDsl % "it")

lazy val weakSqlDsl =
  project.in(file("./plugins/dsl/mild-sql-dsl"))
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(scoobieSettings ++ publishSettings)
    .settings(name := "scoobie-contrib-mild-sql-dsl")
    .settings(description := "Introduces a weakly typed SQL DSL to scoobie.")
    .settings(libraryDependencies += specs)
    .settings(packageInfoGenerator("scoobie.dsl.weaksql", "scoobie-contrib-mild-sql-dsl"))
    .dependsOn(core)

lazy val ansiSql =
  project.in(file("./plugins/dialects/ansi-sql"))
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings)
    .settings(name := "scoobie-contrib-ansi-sql")
    .settings(description := "Provides an ANSI-SQL interpreter for use with the Scoobie AST.")
    .settings(libraryDependencies ++= Seq(scalaz, specs))
    .settings(packageInfoGenerator("scoobie.doobie.doo.ansi", "scoobie-ansi-sql"))
    .settings(scoobieSettings)
    .dependsOn(core, weakSqlDsl % "test")

enablePlugins(MicrositesPlugin)

lazy val docs =
  project.in(file("doc"))
    .enablePlugins(TutPlugin, MicrositesPlugin)
    .settings(scoobieSettings ++ noPublishSettings)
    .dependsOn(doobiePostgres41, weakSqlDsl, doobieSupport41 % "tut->it;compile->compile;")
    .settings(
      scalacOptions := (scalacOptions in ThisBuild).value.filterNot(_.startsWith("-Ywarn-unused")),
      micrositeName := "Scoobie",
      micrositeDescription := "A set of DSLs for querying with Doobie.",
      micrositeAuthor := "Jacob Barber",
      micrositeHomepage := "scoobie.jacoby6000.com",
      micrositeOrganizationHomepage := "jacoby6000.com",
      micrositeBaseUrl := "/scoobie",
      micrositeGithubOwner := "jacoby6000",
      micrositeGithubRepo := "scoobie",
      micrositePushSiteWith := GitHub4s,
      micrositeGithubToken := Some(sys.env("GITHUB_MICROSITES_TOKEN")),
      micrositeHighlightTheme := "atom-one-light",
      micrositePalette := Map(
        "brand-primary"     -> "#E05236",
        "brand-secondary"   -> "#3F3242",
        "brand-tertiary"    -> "#2D232F",
        "gray-dark"         -> "#453E46",
        "gray"              -> "#837F84",
        "gray-light"        -> "#E3E2E3",
        "gray-lighter"      -> "#F4F3F4",
        "white-color"       -> "#FFFFFF"
      )
    )
