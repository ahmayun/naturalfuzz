#!/bin/bash
# NOTE: RUN 'sbt assembly' FIRST IF YOU MADE CHANGES TO THE CODE
# NOTE: If you get scala.reflect.internal.MissingRequirementError, ensure that the machine is running java 8 when 'java' is invoked

# . Decide an output directory for coverage output
# . Run ScoverageInstrumenter.scala using the assembled jar file
# . Add scoverage-instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# . Run a subject program e.g. examples.fuzzable.FlightDistance
# . Process the measurement files produced using CoverageMeasurementConsolidator.scala

# SAMPLE RUN:
#       ./run-bigfuzz.sh DeliveryFaults faulty 86400; echo "Subject: $(hostname): bigfuzz (exit $?)" | sendmail ahmad35@vt.edu

# Temporarily hard-coded, should be parsed from args
NAME=$1
MUTANT_NAME=$2
PACKAGE=$3
DURATION=$4
shift 4
DATASETS=$@

#CLASS_INSTRUMENTED=examples.fuzzable.$NAME # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/$PACKAGE/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/$PACKAGE/$NAME*"
DIR_BIGFUZZ_OUT="target/bigfuzz-output/$MUTANT_NAME"

rm -rf $DIR_BIGFUZZ_OUT
mkdir -p $DIR_BIGFUZZ_OUT/{scoverage-results,report,log,reproducers,crashes} || exit 1


#sbt assembly || exit 1

java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_BIGFUZZ_OUT/scoverage-results/referenceProgram \
          || exit

pushd target/scala-2.12/classes || exit 1
jar uvf  ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES \
        || exit 1
popd || exit 1

START_TIME=$(date +"%T %D")
#echo -e "Subject:[START] BigFuzz $(hostname)\n$NAME $START_TIME" | sendmail ahmad35@vt.edu
date > $DIR_BIGFUZZ_OUT/start.time

java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar \
          runners.RunBigFuzzJar \
          $NAME \
          $MUTANT_NAME \
          $DURATION \
          $DIR_BIGFUZZ_OUT\
          $DATASETS

date > $DIR_BIGFUZZ_OUT/end.time
#echo -e "Subject:[END] BigFuzz $(hostname)\n$NAME $START_TIME exit $?" | sendmail ahmad35@vt.edu