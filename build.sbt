import UnidocKeys._
import ReleaseTransformations._
import ScoobieUtil._

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/"

lazy val scoobie =
  project.in(file("./.root"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobie")
    .settings(scoobieSettings ++ noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(docs))
    .dependsOn(ansiAst, doobieSupport, doobiePostgres, dslAnsiSqlSchemaless, ansiSql, postgresConnection)
    .aggregate(ansiAst, doobieSupport, doobiePostgres, dslAnsiSqlSchemaless, ansiSql, postgresConnection)
    .settings(
      publishAllSigned :=
        Def.sequential(
          (PgpKeys.publishSigned in ansiAst),
          (PgpKeys.publishSigned in dslAnsiSqlSchemaless),
          (PgpKeys.publishSigned in ansiSql),
          (PgpKeys.publishSigned in postgresConnection)
          (PgpKeys.publishSigned in doobieSupport40),
          (PgpKeys.publishSigned in doobieSupport41),
          (PgpKeys.publishSigned in doobiePostgres40),
          (PgpKeys.publishSigned in doobiePostgres41),
          (PgpKeys.publishSigned in doobieMySql41),
          (PgpKeys.publishSigned in doobieMySql40),
        ).value
    )

lazy val dslAnsiSqlSchemaless =
  project.in(file("dsl/schemaless/ansi/sql"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(scoobieSettings ++ publishSettings("scoobie.dsl.schemaless.ansi.sql"))
    .settings(name := "scoobie-dsl-schemaless-ansi-sql")
    .settings(description := "Introduces a schemaless SQL DSL to scoobie.")
    .settings(libraryDependencies += specsNoIt)
    .dependsOn(ansiAst)

lazy val ansiSql =
  project.in(file("interpreters/ansi"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(publishSettings("scoobie.doobie.ansi"))
    .settings(name := "scoobie-interpreters-ansi")
    .settings(description := "Provides an ANSI-SQL interpreter for use with the Scoobie AST.")
    .settings(libraryDependencies ++= Seq(scalaz, specsNoIt))
    .settings(scoobieSettings)
    .dependsOn(ansiAst, dslAnsiSqlSchemaless % "test")

lazy val postgresConnection =
  project.in(file("connection/postgres"))
    .enablePlugins(SbtOsgi)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings("doobie.connection.postgres"))
    .settings(name := "scoobie-connection-postgres")
    .settings(description := "Provides database connection interface for postgres")
    .settings(libraryDependencies ++= Seq(scodecCore, scalazCore, scodecScalaz, specs))
    .settings(scoobieSettings)

lazy val docs =
  project.in(file("doc"))
    .enablePlugins(TutPlugin, MicrositesPlugin)
    .settings(scoobieSettings ++ noPublishSettings)
    .dependsOn(doobiePostgres41, dslAnsiSqlSchemaless, doobieSupport41 % "tut->it;compile->compile;")
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

/*
 * Doobie stuff below
 */

lazy val scoobieDoobie40 =
  project.in(file("./.dummy/scoobieDoobie40"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobieDoobie40")
    .settings(scoobieSettings ++ noPublishSettings)
    .aggregate(ansiAst, doobieSupport40, doobiePostgres40, doobieMySql40, dslAnsiSqlSchemaless)

lazy val scoobieDoobie41 =
  project.in(file("./.dummy/scoobieDoobie41"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "scoobieDoobie41")
    .settings(scoobieSettings ++ noPublishSettings)
    .aggregate(ansiAst, doobieSupport41, doobiePostgres41, doobieMySql41, dslAnsiSqlSchemaless)

lazy val ansiAst =
  project.in(file("ast/ansi"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(name := "scoobie-ast-ansi")
    .settings(description := "AST for making convenient SQL DSLs in Scala.")
    .settings(scoobieSettings ++ publishSettings("scoobie"))
    .settings(libraryDependencies ++= Seq(specsNoIt))

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
    .dependsOn(ansiAst)

lazy val doobieSupport41 =
  project.in(doobieCoreFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(doobieCoreSettings.tail.head)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(publishSettings("scoobie.doobie"))
    .dependsOn(ansiAst)

lazy val doobieSupport40 =
  project.in(doobieCoreFile)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(doobieCoreSettings.tail.tail.head)
    .settings(publishSettings("scoobie.doobie"))
    .dependsOn(ansiAst)

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
    .dependsOn(doobieSupport % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")

lazy val doobiePostgres41 =
  project.in(doobiePgFile)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings("scoobie.doobie.postgres"))
    .settings(doobiePgSettings.tail.head)
    .dependsOn(doobieSupport41 % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")

lazy val doobiePostgres40 =
  project.in(doobiePgFile)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings("scoobie.doobie.postgres"))
    .settings(doobiePgSettings.tail.tail.head)
    .dependsOn(doobieSupport40 % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")

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
    .dependsOn(doobieSupport % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")

lazy val doobieMySql41 =
  project.in(doobieMySqlFile)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings("scoobie.doobie.mysql"))
    .settings(doobieMySqlSettings.tail.head)
    .dependsOn(doobieSupport41 % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")

lazy val doobieMySql40 =
  project.in(doobieMySqlFile)
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(publishSettings("scoobie.doobie.mysql"))
    .settings(doobieMySqlSettings.tail.tail.head)
    .dependsOn(doobieSupport40 % "compile->compile;it->it;", ansiSql, dslAnsiSqlSchemaless % "it")
