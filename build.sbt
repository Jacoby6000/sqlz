import ReleaseTransformations._
import SqlzUtil._


lazy val sqlz =
  project.in(file("./.root"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "sqlz")
    .settings(sqlzSettings ++ noPublishSettings)
    .dependsOn(ansiTagless, ansiAlgebra, data)
    .aggregate(ansiTagless, ansiAlgebra, data)
    .settings(
      publishAllSigned :=
        Def.sequential(
          PgpKeys.publishSigned in ansiTagless,
          PgpKeys.publishSigned in data
        ).value
    )

lazy val ansiTagless =
  project.in(file("sqlz-tagless/ansi"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(name := "sqlz-tagless-ansi")
    .settings(description := "")
    .settings(sqlzSettings ++ publishSettings("sqlz"))
    .settings(libraryDependencies ++= Seq(specsNoIt))

lazy val ansiAlgebra =
  project.in(file("sqlz-algebra/ansi"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(name := "sqlz-algebra-ansi")
    .settings(description := "")
    .settings(sqlzSettings ++ publishSettings("sqlz"))
    .settings(libraryDependencies ++= Seq(scalaz, specsNoIt))
    .dependsOn(ansiTagless)

lazy val jdbc=
  project.in(file("sqlz-jdbc"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(name := "sqlz-jdbc")
    .settings(description := "")
    .settings(sqlzSettings ++ publishSettings("sqlz"))
    .settings(libraryDependencies ++= Seq(scalaz, specsNoIt))
    .dependsOn(ansiTagless)

lazy val data =
  project.in(file("sqlz-data"))
    .enablePlugins(SbtOsgi)
    .settings(name := "sqlz-data")
    .settings(description := "Data structures for sqlz.")
    .settings(sqlzSettings ++ publishSettings("sqlz"))
    .settings(libraryDependencies ++= Seq(specsNoIt))
