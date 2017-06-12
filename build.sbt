/**
  * Large portions of this build are based on @tpolecat's (Rob Norris) build file for doobie. Any genius found here is courtesy of him.
  */

import UnidocKeys._
import ReleaseTransformations._
import OsgiKeys._

lazy val buildSettings = Seq(
  scalaVersion := "2.12.1",
  organization := "com.github.jacoby6000",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  crossScalaVersions := Seq("2.11.8", scalaVersion.value)
)



lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8", // 2 args
    "-feature",
    "-language:existentials",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-language:experimental.macros",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-value-discard",
    "-Xmax-classfile-name", "128"
//    "-Ypatmat-exhaust-depth", "off"
  ),
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
    "-doc-source-url", "https://github.com/jacoby6000/scoobie/tree/v" + version.value + "${FILE_PATH}.scala"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
)

lazy val publishSettings = osgiSettings ++ Seq(
  exportPackage := Seq("scoobie.*"),
  privatePackage := Seq(),
  dynamicImportPackage := Seq("*"),
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/jacoby6000/scoobie")),
  pomIncludeRepository := Function.const(false),
  pomExtra :=
  <scm>
    <url>git@github.com:Jacoby6000/scoobie.git</url>
    <connection>scm:git:git@github.com:Jacoby6000/scoobie.git</connection>
  </scm>
  <developers>
    <developer>
      <id>Jacoby6000</id>
      <name>Jacob Barber</name>
      <url>http://jacoby6000.github.com/</url>
      <email>Jacoby6000@gmail.com</email>
    </developer>
  </developers>,
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    ReleaseStep(action = Command.process("package", _)),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges
  )
)

lazy val scoobieSettings = buildSettings ++ commonSettings ++ tutSettings

lazy val scoobie =
  project.in(file("."))
    .settings(name := "scoobie")
    .settings(scoobieSettings ++ noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(docs))
    .dependsOn(core, doobiePostgres, weakSqlDsl)
    .aggregate(core, doobieSupport, doobieSupport40, doobieSupport41, doobiePostgres, doobiePostgres40, doobiePostgres41, weakSqlDsl, docs, ansiSql)
    .settings(
      tutcp <<= (tut map { a =>

        val src = file(".") / "doc" / "target" / "scala-2.11" / "tut" / "readme.md"
        val dst = file(".") / "readme.md"

        println("Copying " + src + " to " + dst)

        if (src.exists())
          IO.copy(List(src -> dst), overwrite = true, preserveLastModified = true)
        else
          println("No tut output found at" + src.toString)

        a
      })
    )

lazy val core =
  project.in(file("core"))
    .enablePlugins(SbtOsgi)
    .settings(name := "scoobie-core")
    .settings(description := "AST for making convenient SQL DSLs in Scala.")
    .settings(scoobieSettings ++ publishSettings)
    .settings(libraryDependencies ++= Seq(specs))
    .settings(packageInfoGenerator("scoobie", "scoobie-core"))

lazy val doobieSupport =
  project.in(file("doobie-support"))
    .settings(scoobieSettings)
    .settings(description := "Introduces doobie support to scoobie.")
    .settings(noPublishSettings)
    .settings(libraryDependencies ++= Seq(doobieCore % doobieVersion40))
    .dependsOn(core)

lazy val doobieSupport40 =
  project.in(file("doobie-support"))
    .enablePlugins(SbtOsgi)
    .settings(target := file("doobie-support").getAbsoluteFile / "target40")
    .settings(publishSettings)
    .settings(name := "scoobie-contrib-doobie40-support")
    .settings(description := "Introduces doobie support to scoobie.")
    .settings(libraryDependencies ++= Seq(doobieCore % doobieVersion40, specs))
    .settings(packageInfoGenerator("scoobie.doobie", "scoobie-doobie40-support"))
    .settings(scoobieSettings)
    .dependsOn(core)

lazy val doobieSupport41 =
  project.in(file("doobie-support"))
    .enablePlugins(SbtOsgi)
    .settings(target := file("doobie-support").getAbsoluteFile / "target41")
    .settings(publishSettings)
    .settings(name := "scoobie-contrib-doobie41-support")
    .settings(description := "Introduces doobie support to scoobie.")
    .settings(libraryDependencies ++= Seq(doobieCore % doobieVersion41, specs))
    .settings(packageInfoGenerator("scoobie.doobie", "scoobie-doobie41-support"))
    .settings(scoobieSettings)
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

lazy val doobiePostgres =
  project.in(file("doobie-postgres"))
    .settings(noPublishSettings)
    .settings(scoobieSettings)
    .settings(description := "Introduces doobie support to scoobie with postgres.")
    .settings(libraryDependencies ++= Seq(doobiePGDriver % doobieVersion41, specs))
    .dependsOn(doobieSupport, ansiSql, weakSqlDsl % "test")

lazy val doobiePostgres40 =
  project.in(file("doobie-postgres"))
    .enablePlugins(SbtOsgi)
    .settings(target := file("doobie-postgres").getAbsoluteFile / "target40")
    .settings(publishSettings)
    .settings(scoobieSettings)
    .settings(name := "scoobie-contrib-doobie40-postgres")
    .settings(description := "Introduces doobie support to scoobie with postgres.")
    .settings(libraryDependencies ++= Seq(doobiePGDriver % doobieVersion40, specs))
    .settings(packageInfoGenerator("scoobie.doobie.postgres", "scoobie-contrib-doobie40-postgres"))
    .dependsOn(doobieSupport40, ansiSql, weakSqlDsl % "test")

lazy val doobiePostgres41 =
  project.in(file("doobie-postgres"))
    .enablePlugins(SbtOsgi)
    .settings(target := file("doobie-postgres").getAbsoluteFile / "target41")
    .settings(publishSettings)
    .settings(scoobieSettings)
    .settings(name := "scoobie-contrib-doobie41-postgres")
    .settings(description := "Introduces doobie support to scoobie with postgres.")
    .settings(libraryDependencies ++= Seq(doobiePGDriver % doobieVersion41, specs))
    .settings(packageInfoGenerator("scoobie.doobie.postgres", "scoobie-contrib-doobie41-postgres"))
    .dependsOn(doobieSupport41, ansiSql, weakSqlDsl % "test")


lazy val weakSqlDsl =
  project.in(file("mild-sql-dsl"))
    .enablePlugins(SbtOsgi)
    .settings(scoobieSettings ++ publishSettings)
    .settings(name := "scoobie-contrib-mild-sql-dsl")
    .settings(description := "Introduces a weakly typed SQL DSL to scoobie.")
    .settings(libraryDependencies += specs)
    .settings(packageInfoGenerator("scoobie.dsl.weaksql", "scoobie-contrib-mild-sql-dsl"))
    .dependsOn(core)


lazy val docs =
  project.in(file("doc"))
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


lazy val doobieVersion40 = "0.4.0"
lazy val doobieVersion41 = "0.4.1"
lazy val doobieCore = "org.tpolecat" %% "doobie-core"
lazy val doobieCoreCats = "org.tpolecat" %% "doobie-core-cats"
lazy val doobiePGDriver = "org.tpolecat" %% "doobie-postgres"
lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.10"

lazy val specs = "org.specs2" %% "specs2-core" % "3.8.8" % "test"

lazy val ctut = taskKey[Unit]("Copy tut output to blog repo nearby.")

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val tutcp = taskKey[Seq[(sbt.File, String)]]("Copy tut readme output to projroot/readme.md")

def packageInfoGenerator(packageName: String, artifactName: String) =
  sourceGenerators in Compile += Def.task {
    val outDir = (sourceManaged in Compile).value / artifactName
    val outFile = new File(outDir, "buildinfo.scala")
    outDir.mkdirs
    val v = version.value
    val t = System.currentTimeMillis
    IO.write(outFile,
      s"""|package $packageName
          |
          |/** Auto-generated build information. */
          |object buildinfo {
          |  /** Current version of $artifactName ($v). */
          |  val version = "$v"
          |  /** Build date (${new java.util.Date(t)}). */
          |  val date    = new java.util.Date(${t}L)
          |}
          |""".stripMargin)
    Seq(outFile)
  }.taskValue
