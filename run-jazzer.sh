# sbt assembly
sudo docker run -v $(pwd)/target/scala-2.11:/fuzzing -v $(pwd)/seeds:/seeds cifuzz/jazzer --cp=/fuzzing/ProvFuzz-assembly-1.0.jar --target_class=jazzer.DummyFuzzTarget
