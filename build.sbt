/**
  * Large portions of this build are based on @tpolecat's (Rob Norris) build file for doobie. Any genius found here is courtesy of him.
  */

import UnidocKeys._
import ReleaseTransformations._
import OsgiKeys._

lazy val buildSettings = Seq(
  scalaVersion := "2.11.8",
  organization := "com.github.jacoby6000",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  crossScalaVersions := Seq("2.10.5", scalaVersion.value)
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
  ),
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups",
    "-sourcepath", (baseDirectory in LocalRootProject).value.getAbsolutePath,
    "-doc-source-url", "https://github.com/jacoby6000/scoobie/tree/v" + version.value + "€{FILE_PATH}.scala"
  ),
  libraryDependencies ++= Seq(
    "org.specs2" %% "specs2-core" % "3.8" % "test",
    "org.scalacheck" %% "scalacheck" % "1.13.0" % "test",
    "org.typelevel" %% "shapeless-scalacheck" % "0.4",
    "org.typelevel" %% "shapeless-scalaz" % "0.4"
  ),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")
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

lazy val root =
  project.in(file("."))
    .settings(name := "root")
    .settings(scoobieSettings)
    .settings(noPublishSettings)
    .settings(unidocSettings)
    .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(docs))
    .dependsOn(core, postgres, docs)
    .aggregate(core, postgres, docs)
    .settings(
      tutcp <<= tut andFinally {
          val src = file(".") / "doc" / "target" / "scala-2.11" / "tut" / "readme.md"
          val dst = file(".") / "readme.md"

          println("Moving " + src + " to " + dst)

          if (src.exists())
            IO.copy(List(src -> dst), overwrite = true, preserveLastModified = true)
          else
            println("No tut output found at" + src.toString)
      }
    )

lazy val core =
  project.in(file("core"))
    .enablePlugins(SbtOsgi)
    .settings(name := "scoobie-core")
    .settings(description := "AST for making convenient SQL DSLs in Scala.")
    .settings(scoobieSettings ++ publishSettings)
    .settings(libraryDependencies += shapeless)
    .settings(packageInfoGenerator("scoobie", "scoobie-core"))

lazy val docs =
  project.in(file("doc"))
    .settings(scoobieSettings)
    .settings(noPublishSettings)
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
    .dependsOn(core, postgres)

lazy val doobieSupport =
  project.in(file("doobie-support"))
    .settings(scoobieSettings)
    .settings(noPublishSettings)
    .settings(
      libraryDependencies += doobieCore
    )
    .dependsOn(core)

lazy val postgres =
  project.in(file("postgres"))
    .settings(scoobieSettings)
    .settings(noPublishSettings)
    .settings(name := "scoobie-contrib-postgres")
    .settings(description := "Introduces doobie support to scoobie with postgres.")
    .settings(libraryDependencies += doobiePGDriver)
    .settings(packageInfoGenerator("scoobie.doobie.postgres", "scoobie-contrib-postgres"))
    .dependsOn(core, doobieSupport)


lazy val doobieVersion = "0.3.0-M1"
lazy val shapeless = "com.chuusai" %% "shapeless" % "2.3.1"
lazy val doobieCore = "org.tpolecat" %% "doobie-core" % doobieVersion
lazy val doobiePGDriver = "org.tpolecat" %% "doobie-contrib-postgresql" % doobieVersion


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