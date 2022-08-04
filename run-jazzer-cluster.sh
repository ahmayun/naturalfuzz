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
MODE=$2
DURATION=$3
PACKAGE=$4
CLASS_TARGET=jazzer.JazzerTarget$NAME
#CLASS_INSTRUMENTED=examples.fuzzable.$NAME # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/$PACKAGE/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/$PACKAGE/$NAME*"
DIR_JAZZER_OUT="target/jazzer-output/$NAME"

rm -rf $DIR_JAZZER_OUT
rm -rf target/inputs/{ds1,ds2}
mkdir -p $DIR_JAZZER_OUT/{measurements,report,log,reproducers,crashes} || exit 1
./crash-checker.sh target/jazzer-output/WebpageSegmentation/reproducers/ &


sbt assembly || exit 1

java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_JAZZER_OUT/measurements \
          || exit 1


mkdir -p target/scala-2.11/$DIR_JAZZER_OUT/measurements
cp $DIR_JAZZER_OUT/measurements/scoverage.coverage target/scala-2.11/$DIR_JAZZER_OUT/measurements || exit 1

pushd target/scala-2.11/classes || exit
jar uvf  ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES \
        || exit 1
popd || exit 1

#java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
#          $CLASS_INSTRUMENTED \
#          seeds/weak_seed/webpage_segmentation/*

timeout $DURATION docker run -v "$(pwd)"/target/scala-2.11:/fuzzing \
                -v "$(pwd)"/seeds:/seeds \
                -v "$(pwd)"/$DIR_JAZZER_OUT/reproducers:/reproducers \
                -v "$(pwd)"/$DIR_JAZZER_OUT/log:/log \
                -v "$(pwd)"/target/inputs:/inputs \
                cifuzz/jazzer \
                -len_control=0 \
                --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
                --target_class=$CLASS_TARGET \
                --reproducer_path=/reproducers \
                --log_dir=/log \
                --target_args="$DIR_JAZZER_OUT/measurements $MODE" \
                --keep_going=200

kill $(ps -e | grep inotifywait | sed -e 's/\([0-9]\+\).\+/\1/')

mv target/scala-2.11/crash* $DIR_JAZZER_OUT/crashes
mv target/scala-2.11/$DIR_JAZZER_OUT/measurements/* $DIR_JAZZER_OUT/measurements

rm -rf target/scala-2.11/target

java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.CoverageMeasurementConsolidator \
          $DIR_JAZZER_OUT/measurements \
          src/main/scala \
          $DIR_JAZZER_OUT/report

