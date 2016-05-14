name := "dooqie"

scalaVersion := "2.11.8"

organization := "com.github.jacoby6000"

version := "0.0.2"


libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.0",
  "org.tpolecat" %% "doobie-core" % "0.3.0-M1",
  "org.tpolecat" %% "doobie-contrib-postgresql" % "0.3.0-M1"
)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.7.1")

scalacOptions ++= Seq(
//  "-Yno-predef",   // no automatic import of Predef (removes irritating implicits)
  "-deprecation",
  "-encoding", "UTF-8",       // yes, this is 2 args
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",        // N.B. doesn’t work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
//  "-Ywarn-unused-import"     // 2.11 only
)

tutSettings
