package runners

import fuzzer.{Fuzzer, Program}
import guidance.RandomGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}

import java.io.File

object RunRandFuzz {

  def main(args: Array[String]): Unit = {

    val runs = Config.iterations

    // ==P.U.T. dependent configurations=======================
    val benchmark_name = Config.benchmarkName
    val Some(input_files) = Config.mapInputFiles.get(benchmark_name)
    val Some(fun_fuzzable) = Config.mapFunFuzzables.get(benchmark_name)
    val Some(schema) = Config.mapSchemas.get(benchmark_name)
    val benchmark_class = Config.benchmarkClass
    // ========================================================

    val guidance = new RandomGuidance(input_files, schema, runs)
    val benchmark_path = s"src/main/scala/${benchmark_class.split('.').mkString("/")}.scala"
    val output_dir = s"${Config.resultsDir}/BigFuzz/scoverage-results"
    val program = new Program(benchmark_name,
      benchmark_class,
      benchmark_path,
      fun_fuzzable,
      input_files)

    // Preprocessing and Fuzzing
    val (stats, ts_fuzz, te_fuzz) = Fuzzer.Fuzz(program, guidance, output_dir)

    val coverage = Serializer.deserialize(new File(s"$output_dir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(output_dir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(output_dir)).write(coverage)

    val fuzz_time = (te_fuzz - ts_fuzz) / 1000.0

    // Printing results
    stats.failureMap.foreach { case (msg, (_, c, _)) => println(s"$c x $msg") }
    println(s"=== RESULTS: RandFuzz $benchmark_name ===")
    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    println(s"iterations: ${stats.plotData._1.mkString(",")}")
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
    println(s"Total Time (s): ${limitDP(fuzz_time, 2)}")
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
        s"\tIterations: $runs"
    )
  }

  def limitDP(d: Double, dp: Int): Double = {
    BigDecimal(d).setScale(dp, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}
