#! /bin/bash
sbt ++$TRAVIS_SCALA_VERSION compile coverageReport test
