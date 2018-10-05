logLevel := Level.Warn

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("org.tpolecat"      % "tut-plugin"     % "0.6.7")
addSbtPlugin("com.47deg"         % "sbt-microsites" % "0.7.23")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"  % "1.6.0-M4")
addSbtPlugin("com.eed3si9n"      % "sbt-unidoc"     % "0.4.2")
addSbtPlugin("com.jsuereth"      % "sbt-pgp"        % "1.1.2")
addSbtPlugin("com.github.gseitz" % "sbt-release"    % "1.0.9")
addSbtPlugin("org.xerial.sbt"    % "sbt-sonatype"   % "2.3"  )
addSbtPlugin("com.typesafe.sbt"  % "sbt-osgi"       % "0.9.4")
addSbtPlugin("com.typesafe.sbt"  % "sbt-ghpages"    % "0.6.2")
addSbtPlugin("com.eed3si9n"      % "sbt-buildinfo"  % "0.9.0")
