
# NOTE: RUN 'sbt assembly' FIRST IF YOU MADE CHANGES TO THE CODE
# NOTE: If you get scala.reflect.internal.MissingRequirementError, ensure that the machine is running java 8 when 'java' is invoked

# . Decide an output directory for coverage output
# . Run ScoverageInstrumenter.scala using the assembled jar file
# . Add scoverage-instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# . Run a subject program e.g. examples.fuzzable.FlightDistance
# . Process the measurement files produced using CoverageMeasurementConsolidator.scala


# Temporarily hard-coded, should be parsed from args
NAME=WebpageSegmentation
CLASS_TARGET=jazzer.$NAME
CLASS_INSTRUMENTED=examples.fuzzable.$NAME # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/fuzzable/$NAME.scala"
PATH_INSTRUMENTED_CLASSES="examples/fuzzable/$NAME*"
DIR_JAZZER_OUT="target/jazzer-output/$CLASS_TARGET"

rm -rf $DIR_JAZZER_OUT
mkdir -p $DIR_JAZZER_OUT/{measurements,report}


java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_JAZZER_OUT/measurements

pushd target/scala-2.11/classes || exit
jar uvf  ../ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES
popd

#java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
#          $CLASS_INSTRUMENTED \
#          seeds/weak_seed/webpage_segmentation/*

sudo docker run -v "$(pwd)"/target/scala-2.11:/fuzzing \
                -v "$(pwd)"/seeds:/seeds cifuzz/jazzer \
                --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
                --target_class=$CLASS_TARGET

#java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
#          utils.CoverageMeasurementConsolidator \
#          target/jazzer-output/$CLASS_TARGET/measurements \
#          src/main/scala \
#          $DIR_JAZZER_OUT/report

