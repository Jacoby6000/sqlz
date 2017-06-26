#! /bin/bash

if [ "$1" = "scoobie" ]; then
    sbt clean coverage $2 coverageReport $3 coverageReport coverageAggregate
elif [ "$1" = "tut" ]; then
    sbt docs/tut
else
    sbt $1/clean $1/$2
fi
