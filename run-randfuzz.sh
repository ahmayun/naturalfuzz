#!/bin/bash
# NOTE: RUN 'sbt assembly' FIRST IF YOU MADE CHANGES TO THE CODE
# NOTE: If you get scala.reflect.internal.MissingRequirementError, ensure that the machine is running java 8 when 'java' is invoked

# . Decide an output directory for coverage output
# . Run ScoverageInstrumenter.scala using the assembled jar file
# . Add scoverage-instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# . Run a subject program e.g. examples.fuzzable.FlightDistance
# . Process the measurement files produced using CoverageMeasurementConsolidator.scala


# Temporarily hard-coded, should be parsed from args
NAME=$1
PACKAGE=$2
DURATION=$3

#CLASS_INSTRUMENTED=examples.fuzzable.$NAME # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/$PACKAGE/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/$PACKAGE/$NAME*"
DIR_RANDFUZZ_OUT="target/randfuzz-output/$NAME"

rm -rf $DIR_RANDFUZZ_OUT
mkdir -p $DIR_RANDFUZZ_OUT/{scoverage-results,report,log,reproducers,crashes} || exit 1


sbt assembly || exit 1

java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_RANDFUZZ_OUT/scoverage-results \
          || exit

pushd target/scala-2.11/classes || exit 1
jar uvf  ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES \
        || exit 1
popd || exit 1


java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          runners.RunRandFuzzJar \
          $NAME \
          $PACKAGE \
          $DURATION \
          $DIR_RANDFUZZ_OUT
