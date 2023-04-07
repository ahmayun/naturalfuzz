#!/bin/bash

# SAMPLE RUN:
#       ./run-rigfuzz.py FlightDistance faulty 86400 --email=ahmad35@vt.edu --compile=True

exitScript() {
    mv ~/jazzerresults src/main/scala
    exit 1;
}

mv src/main/scala/jazzerresults ~ # sbt gets stuck in infinite loop so move this out of directory
sbt assembly || exitScript
mv ~/jazzerresults src/main/scala

NAME=$1
MUTANT_NAME=$2
PACKAGE=$3
DURATION=$4
shift 4
DATASETS=$@

PATH_SCALA_SRC="src/main/scala/examples/$PACKAGE/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/$PACKAGE/$NAME*"
DIR_RIGFUZZ_OUT="target/RIG-output/$NAME"

rm -rf $DIR_RIGFUZZ_OUT
mkdir -p $DIR_RIGFUZZ_OUT/{scoverage-results,report,log,reproducers,crashes} || exit 1


# sbt assembly || exit 1

java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_RIGFUZZ_OUT/scoverage-results

pushd target/scala-2.12/classes || exit 1
jar uvf ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES \
        || exit 1
popd || exit 1

date > $DIR_RIGFUZZ_OUT/start.time

java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar \
          runners.RunRIGMutantJarFuzzing \
          $NAME \
          $MUTANT_NAME \
          local[*] \
          $DURATION \
          $DIR_RIGFUZZ_OUT \
          $DATASETS

date > $DIR_RIGFUZZ_OUT/end.time
