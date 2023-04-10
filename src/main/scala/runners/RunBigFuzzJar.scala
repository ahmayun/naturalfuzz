package runners

import fuzzer.{Fuzzer, Global, NewFuzzer, Program}
import guidance.BigFuzzGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}

import java.io.File

object RunBigFuzzJar {

  def main(args: Array[String]): Unit = {

    // ==P.U.T. dependent configurations=======================
    val (benchmark_name, mutantName, input_files, duration, outDir) =
      if (!args.isEmpty) {
        (args(0),
          args(1),
          args.takeRight(args.length - 4),
          args(2),
          args(3))
      } else {
//        val name = "Q3"
//        val _mutantName = "Q3"
//        (name,
//          _mutantName,
//          Array("store_sales", "date_dim", "item").map { s => s"seeds/bigfuzz/Q3/$s" },
//          "20",
//          s"target/bigfuzz-output/$name")
        val name = "WebpageSegmentation"
        val _mutantName = "WebpageSegmentation_M19_83_lte_neq"
        (name,
          _mutantName,
          Array("dataset_0", "dataset_1").map { s => s"./seeds/rig_reduced_data/$name/$s" },
          "20",
          s"target/bigfuzz-output/${name}")
      }

    val Some(fun_fuzzable) = Config.mapFunFuzzables.get(benchmark_name)
    val Some(schema) = Config.mapSchemas.get(benchmark_name)
    val benchmark_class = s"examples.faulty.$benchmark_name"
    // ========================================================
    val mutantClass = s"examples.mutants.$benchmark_name.$mutantName"
    val mutantPath = s"src/main/scala/${mutantClass.split('.').mkString("/")}.scala"
    val Some(funMutant) = Config.mapFunMutants.get(mutantName) // getMainFunctionDynamically(mutantClass)

    val guidance = new BigFuzzGuidance(input_files, schema, duration.toInt)
    val benchmark_path = s"src/main/scala/${benchmark_class.split('.').mkString("/")}.scala"
    val scoverageOutputDir = s"$outDir/scoverage-results"
    val program = new Program(benchmark_name,
      benchmark_class,
      benchmark_path,
      fun_fuzzable,
      input_files)

    val mutantProgram = new Program(
      mutantName,
      mutantClass,
      mutantPath,
      funMutant,
      input_files)

    // Preprocessing and Fuzzing
    val (stats, ts_fuzz, te_fuzz) = NewFuzzer.FuzzMutants(program, mutantProgram, guidance, outDir)

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
//    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
    println(s"Total Time (s): ${limitDP(fuzz_time, 2)}")
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
        s"\tIterations: ${Global.iteration}\n" +
        s"\tMutation Distribution M1-M6: ${guidance.mutate_probs.mkString(",")}\n" +
        s"\tActual Application: ${guidance.actual_app.mkString(",")}\n"
    )
  }

  def limitDP(d: Double, dp: Int): Double = {
    BigDecimal(d).setScale(dp, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def getLineNo(filename: String, trace: String): String = {
    val pattern = s"""$filename.scala:(\\d+)"""
    pattern.r.findFirstIn(trace) match {
      case Some(str) => str.split(':').last
      case _ => "-1"
    }
  }
}
