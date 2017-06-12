#! /bin/bash

if [ "$1" = "scoobie" ]; then
    sbt clean coverage test coverageReport tut
    bash <(curl -s https://codecov.io/bash)
else
    sbt $1/clean coverage $1/test coverageReport
fi
