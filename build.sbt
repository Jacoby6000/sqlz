/**
  * Large portions of this build are based on @tpolecat's (Rob Norris) build file for doobie. Any genius found here is courtesy of him.
  */

import UnidocKeys._
import ReleaseTransformations._
import SqlzUtil._


lazy val sqlz =
  project.in(file("./.root"))
    .configs(IntegrationTest)
    .settings(Defaults.itSettings)
    .settings(name := "sqlz")
    .settings(sqlzSettings ++ noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject)
    .dependsOn(ansiTagless)
    .aggregate(ansiTagless)
    .settings(
      publishAllSigned :=
        Def.sequential(
          (PgpKeys.publishSigned in ansiTagless)
        ).value
    )

lazy val ansiTagless =
  project.in(file("sqlz-tagless/ansi"))
    .enablePlugins(SbtOsgi/*, BuildInfoPlugin*/)
    .settings(name := "sqlz-tagless-ansi")
    .settings(description := "Tagless interpreters for making convenient SQL DSLs in Scala.")
    .settings(sqlzSettings ++ publishSettings("sqlz"))
    .settings(libraryDependencies ++= Seq(specsNoIt))

/*
lazy val docs =
  project.in(file("docs"))
    .enablePlugins(TutPlugin, MicrositesPlugin)
    .settings(sqlzSettings ++ noPublishSettings)
    .settings(
      scalacOptions := (scalacOptions in ThisBuild).value.filterNot(_.startsWith("-Ywarn-unused")),
      micrositeName := "Scoobie",
      micrositeDescription := "A set of DSLs for querying with Doobie.",
      micrositeAuthor := "Jacob Barber",
      micrositeHomepage := "sqlz.jacoby6000.com",
      micrositeOrganizationHomepage := "jacoby6000.com",
      micrositeBaseUrl := "/sqlz",
      micrositeGithubOwner := "jacoby6000",
      micrositeGithubRepo := "sqlz",
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
    */
