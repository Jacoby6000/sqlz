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
  lazy val specs = "org.specs2" %% "specs2-core" % "3.8.8" % "test"
  lazy val ctut = taskKey[Unit]("Copy tut output to blog repo nearby.")

  lazy val noPublishSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false
  )


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

