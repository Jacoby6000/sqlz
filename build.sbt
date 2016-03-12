name := "dooqie"

scalaVersion := "2.11.7"

organization := "com.github.jacoby6000"

version := "0.0.1"


libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.2.5",
  "org.tpolecat" %% "doobie-core" % "0.2.3",
  "org.tpolecat" %% "doobie-contrib-postgresql" % "0.2.3"
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