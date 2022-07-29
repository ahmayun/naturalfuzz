
# . NOTE: RUN 'sbt assembly' FIRST IF YOU MADE CHANGES TO THE CODE
# . Decide an output directory for coverage output
# . Run ScoverageInstrumenter.scala using the assembled jar file
# . Add scoverage-instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# . Run a subject program e.g. examples.fuzzable.FlightDistance
# . Process the measurement files produced using CoverageMeasurementConsolidator.scala


# Temporarily hard-coded, should be parsed from args
CLASS_TARGET=jazzer.DummyFuzzTarget
CLASS_INSTRUMENTED=examples.fuzzable.FlightDistance # which class needs to be fuzzed DISC vs FWA
PATH_SCALA_SRC="src/main/scala/examples/fuzzable/FlightDistance.scala"
PATH_INSTRUMENTED_CLASSES="target/scala-2.11/classes/examples/fuzzable/FlightDistance.*"
DIR_JAZZER_OUT="target/jazzer-output/$TARGET_CLASS"


java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.ScoverageInstrumenter \
          $PATH_SCALA_SRC \
          $DIR_JAZZER_OUT/measurements

jar uf  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
        $PATH_INSTRUMENTED_CLASSES

java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          $CLASS_INSTRUMENTED

java -cp  target/scala-2.11/ProvFuzz-assembly-1.0.jar \
          utils.CoverageMeasurementConsolidator \
          target/jazzer-output/$CLASS_TARGET/measurements \
          src/main/scala \
          $DIR_JAZZER_OUT/report

sudo docker run -v "$(pwd)"/target/scala-2.11:/fuzzing \
                -v "$(pwd)"/seeds:/seeds cifuzz/jazzer \
                --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
                --target_class=$CLASS_TARGET

