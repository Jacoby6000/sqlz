logLevel := Level.Warn

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.4.2")

resolvers += Resolver.typesafeRepo("releases")

//addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
//addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)