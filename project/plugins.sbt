logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.tpolecat"      % "tut-plugin"     % "0.5.2")
addSbtPlugin("com.47deg"         % "sbt-microsites" % "0.6.1")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"  % "1.5.0")
addSbtPlugin("com.eed3si9n"      % "sbt-unidoc"     % "0.3.1")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"        % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release"    % "1.0.3")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"   % "1.1"  )
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"       % "0.8.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-ghpages"    % "0.6.0")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"  % "0.7.0")
addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.1.4")