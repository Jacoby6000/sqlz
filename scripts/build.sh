#! /bin/bash

if [ "$1" = "scoobie" ]; then
    sbt clean coverage test coverageReport tut
    cmp readme.md doc/target/scala-2.11/tut/readme.md && bash <(curl -s https://codecov.io/bash)
else
    sbt $1/clean coverage $1/test coverageReport
fi
