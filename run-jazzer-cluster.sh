#!/bin/bash
# NOTE: RUN 'sbt assembly' FIRST IF YOU MADE CHANGES TO THE CODE
# NOTE: If you get scala.reflect.internal.MissingRequirementError, ensure that the machine is running java 8 when 'java' is invoked

# . Decide an output directory for coverage output
# . Run ScoverageInstrumenter.scala using the assembled jar file
# . Add scoverage-instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# . Run a subject program e.g. examples.fuzzable.FlightDistance
# . Process the measurement files produced using CoverageMeasurementConsolidator.scala


# SAMPLE RUN:
#       ./run-jazzer-cluster.sh FlightDistance fuzz faulty 86400; echo "Subject: $(hostname): jazzer (exit $?)" | sendmail ahmad35@vt.edu

# Temporarily hard-coded, should be parsed from args
NAME=$1
MUTANT_NAME=$2
MODE=$3
PACKAGE=$4
DURATION=$5
SCALA_VER=2.12

CLASS_TARGET=jazzer.JazzerTarget$NAME
#CLASS_INSTRUMENTED=examples.fuzzable.$NAME # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/$PACKAGE/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/$PACKAGE/$NAME*"
DIR_JAZZER_OUT="target/jazzer-output/$MUTANT_NAME"

rm -rf $DIR_JAZZER_OUT
rm -rf target/inputs/{ds1,ds2}
mkdir -p $DIR_JAZZER_OUT/{measurements,report,log,reproducers,crashes} || exit 1
mkdir -p $DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram || exit 1

./crash-checker.sh 	$DIR_JAZZER_OUT/reproducers/  \
			target/scala-$SCALA_VER/$DIR_JAZZER_OUT/measurements/iter \
			$DIR_JAZZER_OUT/measurements/errors.csv &


exitScript() {
    mv ~/jazzerresults src/main/scala
    exit 1;
}

mv src/main/scala/jazzerresults ~ # sbt gets stuck in infinite loop so move this out of directory
sbt assembly || exitScript

java -cp  target/scala-$SCALA_VER/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram \
          || exit 1


mkdir -p target/scala-$SCALA_VER/$DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram
cp $DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram/scoverage.coverage target/scala-$SCALA_VER/$DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram || exit 1

pushd target/scala-$SCALA_VER/classes || exit
jar uvf  ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES \
        || exit 1
popd || exit 1

#java -cp  target/scala-$SCALA_VER/ProvFuzz-assembly-1.0.jar \
#          $CLASS_INSTRUMENTED \
#          seeds/weak_seed/webpage_segmentation/*

START_TIME=$(date +"%T %D")
#echo -e "Subject:[START] Jazzer $(hostname)\n$NAME $START_TIME" | sendmail ahmad35@vt.edu

timeout $DURATION docker run -v "$(pwd)"/target/scala-$SCALA_VER:/fuzzing \
                -v "$(pwd)"/seeds:/seeds \
                -v "$(pwd)"/$DIR_JAZZER_OUT/reproducers:/reproducers \
                -v "$(pwd)"/target/inputs:/inputs \
                cifuzz/jazzer \
                -len_control=0 \
                --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
                --target_class=$CLASS_TARGET \
                --reproducer_path=/reproducers \
                --target_args="$DIR_JAZZER_OUT/measurements $MODE $PACKAGE $MUTANT_NAME" \
                --keep_going=200
#                -v "$(pwd)"/$DIR_JAZZER_OUT/log:/log \
#                --log_dir=/log \

#echo -e "Subject:[END] Jazzer $(hostname)\n$NAME $START_TIME exit $?" | sendmail ahmad35@vt.edu

chown -R $(whoami):$(whoami) target/scala-$SCALA_VER/target
chown $(whoami):$(whoami) target/scala-$SCALA_VER/crash*

cp -r -v -f target/scala-$SCALA_VER/crash* $DIR_JAZZER_OUT/crashes
cp -r -v -f target/scala-$SCALA_VER/$DIR_JAZZER_OUT/measurements/* $DIR_JAZZER_OUT/measurements

#echo "Removing target/scala-$SCALA_VER/target"
#rm -rf target/scala-$SCALA_VER/target

echo "Consolidating measurements"
java -cp  target/scala-$SCALA_VER/ProvFuzz-assembly-1.0.jar \
          utils.CoverageMeasurementConsolidator \
          $DIR_JAZZER_OUT/measurements/scoverage-results/referenceProgram \
          src/main/scala \
          $DIR_JAZZER_OUT/report


echo "Killing watchers"
kill $(ps -e | grep inotifywait | sed -e 's/\([0-9]\+\).\+/\1/')


# to reproduce errors
#sudo docker run \
#            -v "$(pwd)"/target/scala-$SCALA_VER/fuzzing \
#            -v "$(pwd)"/target/inputs:/inputs \
#            cifuzz/jazzer \
#            --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
#            --target_args="/temp reproduce" \
#            --target_class=jazzer.JazzerTargetWebpageSegmentation \
#            /fuzzing/jazzer-output/WebpageSegmentation/crashes/crash-2ade69a127345e94a05b99f6c37b6160df010806
