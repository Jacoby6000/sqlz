#! /bin/bash

if [ "$1" = "scoobie" ]; then
<<<<<<< HEAD
    sbt clean coverage test coverageReport tut
=======
    sbt clean coverage $2 coverageReport $3 coverageReport coverageAggregate
>>>>>>> master
    bash <(curl -s https://codecov.io/bash)
else
    sbt $1/clean $1/$2
fi
