import sbt._
import sbt.impl._
import Keys._
import com.typesafe.sbt.osgi.SbtOsgi.autoImport._
import com.typesafe.sbt.osgi.SbtOsgi.autoImport.OsgiKeys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import tut.TutPlugin.autoImport._
import com.typesafe.sbt.SbtPgp.autoImport._

object ScoobieUtil {

  lazy val doobieVersions = List("0.4.1", "0.4.0")
  lazy val doobieCore = "org.tpolecat" %% "doobie-core"
  lazy val doobieCoreCats = "org.tpolecat" %% "doobie-core-cats"
  lazy val doobiePGDriver = "org.tpolecat" %% "doobie-postgres"
  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.10"
  lazy val specs = "org.specs2" %% "specs2-core" % "3.8.8" % "test,it"
  lazy val ctut = taskKey[Unit]("Copy tut output to blog repo nearby.")

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )


  lazy val buildSettings = Seq(
    scalaVersion := "2.12.2",
    organization := "com.github.jacoby6000",
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
    crossScalaVersions := Seq("2.11.11", scalaVersion.value)
  )

  lazy val scalacVersionOptions =
    Map(
      "2.12" -> Seq(
        "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
        "-encoding", "utf-8",                // Specify character encoding used by source files.
        "-explaintypes",                     // Explain type errors in more detail.
        "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
        "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
        "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
        "-language:higherKinds",             // Allow higher-kinded types
        "-language:implicitConversions",     // Allow definition of implicit functions called views
        "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
        "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
        "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
        "-Xfuture",                          // Turn on future language features.
        "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
        "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
        "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
        "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
        "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
        "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
        "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
        "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
        "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
        "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
        "-Xlint:option-implicit",            // Option.apply used implicit view.
        "-Xlint:package-object-classes",     // Class or object defined in package object.
        "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
        "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
        "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
        "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
        "-Xlint:unsound-match",              // Pattern match may not be typesafe.
        "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
        "-Ypartial-unification",             // Enable partial unification in type constructor inference
        "-Ywarn-dead-code",                  // Warn when dead code is identified.
        "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
        "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
        "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
        "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
        "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
        "-Ywarn-numeric-widen",              // Warn when numerics are widened.
        "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
        "-Ywarn-unused:locals",              // Warn if a local definition is unused.
        "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
        "-Ywarn-unused:privates",            // Warn if a private member is unused.
        "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
      ),
      "2.11" -> Seq(
        "-encoding", "UTF-8", // 2 args
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
        "-Xmax-classfile-name", "128",
        "-Xfatal-warnings"
      )
    )


  lazy val commonSettings = Seq(
    scalacOptions ++= scalacVersionOptions((scalaVersion in Compile).value.split('.').dropRight(1).mkString(".")),
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

  lazy val scoobieSettings = buildSettings ++ commonSettings


  case class DoobiePlugin(dir: File, settings: List[Seq[Setting[_]]])

  def doobiePlugin(
    doobieArtifact: Option[GroupArtifactID],
    doobiePluginName: String,
    projectDescription: String,
    repeat: Boolean = true,
    doobieVersionList: List[String] = doobieVersions
  ): DoobiePlugin = {
    val sourceDir = s"./plugins/doobie/doobie-$doobiePluginName"
    val settings = doobieVersionList.map { doobieVersion =>
      val versionNoDots = doobieVersion.filterNot(_ == '.')
      val scoobieArtifactName = s"scoobie-contrib-doobie$versionNoDots-$doobiePluginName"
      scoobieSettings ++ Seq(
        name := scoobieArtifactName,
        description := projectDescription,
        libraryDependencies ++= doobieArtifact.map(_ % doobieVersion).toList ++ Seq(specs),
        packageInfoGenerator(s"scoobie.doobie.$doobiePluginName", scoobieArtifactName),
        target := file(sourceDir).getAbsoluteFile / s"target$versionNoDots"
      )
    }

    val result = DoobiePlugin(file(sourceDir), {
      val head =
        if (repeat) {
          Some(
            doobiePlugin(doobieArtifact, doobiePluginName, projectDescription, false, List(doobieVersionList.head)).settings.head ++ Seq(
              target := file(sourceDir).getAbsoluteFile / s"target"
            )
          )
        } else {
          None
        }


      head.toList ::: settings
    })

    result
  }


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

}

