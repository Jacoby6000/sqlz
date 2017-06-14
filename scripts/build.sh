#! /bin/bash

if [ "$1" = "scoobie" ]; then
    sbt clean coverage $2 $3 coverageReport
    bash <(curl -s https://codecov.io/bash)
else
    sbt $1/clean coverage $1/$2 coverageReport
fi
