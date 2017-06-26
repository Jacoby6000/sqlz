#! /bin/bash

sbt clean coverage test coverageReport it:test coverageReport coverageAggregate && sbt scoobieDoobie40/it:test && sbt scoobieDoobie41/it:test && sbt docs/tut
