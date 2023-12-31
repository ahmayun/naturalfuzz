package runners

import fuzzer.{Fuzzer, Program}
import guidance.BigFuzzGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}

import java.io.File

object RunBigFuzz {

  def main(args: Array[String]): Unit = {

    val runs = Config.iterations

    // ==P.U.T. dependent configurations=======================
    val benchmark_name = Config.benchmarkName
    val Some(input_files) = Config.mapInputFiles.get(benchmark_name)
    val Some(fun_fuzzable) = Config.mapFunFuzzables.get(benchmark_name)
    val Some(schema) = Config.mapSchemas.get(benchmark_name)
    val benchmark_class = Config.benchmarkClass
    // ========================================================

    val guidance = new BigFuzzGuidance(input_files, schema, runs)
    val benchmark_path = s"src/main/scala/${benchmark_class.split('.').mkString("/")}.scala"
    val outDir = s"${Config.resultsDir}/BigFuzz"
    val scoverageOutputDir = s"$outDir/scoverage-results"
    val program = new Program(benchmark_name,
      benchmark_class,
      benchmark_path,
      fun_fuzzable,
      input_files)

    // Preprocessing and Fuzzing
    val (stats, ts_fuzz, te_fuzz) = Fuzzer.Fuzz(program, guidance, outDir)

    val coverage = Serializer.deserialize(new File(s"$scoverageOutputDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(scoverageOutputDir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageOutputDir)).write(coverage)

    val fuzz_time = (te_fuzz - ts_fuzz) / 1000.0

    // Printing results
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmark_name, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmark_name, msg.mkString(","))} x $c") }
    stats.failureMap.map { case (msg, (_, c, i)) => (getLineNo(benchmark_name, msg.mkString("\n")), c, i) }
      .groupBy(_._1)
      .map { case (line, list) => (line, list.size) }
      .toList.sortBy(_._1.toInt)
      .foreach(println)

    println(s"=== RESULTS: BigFuzz $benchmark_name ===")
    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    println(s"iterations: ${stats.plotData._1.mkString(",")}")
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
    println(s"Total Time (s): ${limitDP(fuzz_time, 2)}")
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
        s"\tMutation Distribution M1-M6: ${guidance.mutate_probs.mkString(",")}\n" +
        s"\tActual Application: ${guidance.actual_app.mkString(",")}\n" +
        s"\tIterations: $runs"
    )

  }

  def limitDP(d: Double, dp: Int): Double = {
    BigDecimal(d).setScale(dp, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def getLineNo(filename: String, trace: String): String = {
    val pattern = s"""${filename}.scala:(\\d+)"""
    pattern.r.findFirstIn(trace) match {
      case Some(str) => str.split(':').last
      case _ => "-1"
    }
  }
}
