#! /bin/bash
sbt ++$TRAVIS_SCALA_VERSION scoobieDoobie40/it:test && sbt ++$TRAVIS_SCALA_VERSION scoobieDoobie41/it:test && sbt ++$TRAVIS_SCALA_VERSION docs/tut && sbt ++$TRAVIS_SCALA_VERSION clean coverage test coverageReport it:test coverageReport coverageAggregate
