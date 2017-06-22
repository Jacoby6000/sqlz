#! /bin/bash

if [ "$1" = "scoobie" ]; then
    sbt clean coverage $2 coverageReport $3 coverageReport coverageAggregate
    bash <(curl -s https://codecov.io/bash)
else
    sbt $1/clean $1/$2
fi
