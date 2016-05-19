logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.tpolecat"      % "tut-plugin" % "0.4.2")
addSbtPlugin("org.scoverage"     % "sbt-scoverage" % "1.3.5")
addSbtPlugin("com.eed3si9n"      % "sbt-unidoc"   % "0.3.1")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"  % "1.0.3")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype" % "1.1")
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"     % "0.8.0")


//addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
//addCompilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full)