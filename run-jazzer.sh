
# 1. NOTE: RUN sbt assembly FIRST IF YOU MADE CHANGES TO THE CODE
# 2. Run scoverage instrumenter using the assembled jar file
# 3. Add scoverage instrumented class files to jar using 'jar uf target/scala-2.11/ProvFuzz-assembly-1.0.jar target/scala-2.11/examples/fuzzable/<name of instrumented jars>'
# 4.

TARGET_CLASS=jazzer.DummyFuzzTarget

sudo docker run -v "$(pwd)"/target/scala-2.11:/fuzzing \
                -v "$(pwd)"/seeds:/seeds cifuzz/jazzer \
                --cp=/fuzzing/ProvFuzz-assembly-1.0.jar \
                --target_class=$TARGET_CLASS

