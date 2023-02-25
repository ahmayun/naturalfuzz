#!/bin/bash

# example usage:
#   ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzJar /ahmad/FlightDistance/{flights,airports}

exitScript() {
    mv ~/jazzerresults src/main/scala
    exit 1;
}

CLASS=$1
shift
MASTER
ARGS=$@

mv src/main/scala/jazzerresults ~ # sbt gets stuck in infinite loop so move this out of directory
sbt assembly || exitOnFail
mv ~/jazzerresults src/main/scala
cp target/scala-2.12/ProvFuzz-assembly-1.0.jar ~ || exit 1

pushd $SPARK_HOME || exit 1
./copyProvFuzzToNodes.sh ~ ProvFuzz-assembly-1.0.jar
./runProvFuzzJob.sh ~/ProvFuzz-assembly-1.0.jar $CLASS $ARGS
popd || exit 1