package runners

import fuzzer._
import guidance.BigFuzzGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}

import java.io.File

object RunBigFuzzAggErr {

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
    val output_dir = Config.scoverageResultsDir
    val program = new Program(benchmark_name,
      benchmark_class,
      benchmark_path,
      fun_fuzzable,
      input_files)

    // Preprocessing and Fuzzing
    val (all_stats, ts_fuzz, te_fuzz) = (0 until Config.maxRepeats).map {
      i =>
        println(s"Fuzzing trial $i")
        Global.iteration = 0
        Fuzzer.Fuzz(program, guidance, output_dir)
    }.foldLeft((List[FuzzStats](), 0l, 0l)) { case ((list, acc_ts, acc_te), (f, ts, te)) => (list :+ f, acc_ts + ts, acc_te + te) }


    val coverage = Serializer.deserialize(new File(s"$output_dir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(output_dir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(output_dir)).write(coverage)

    val fuzz_time = (te_fuzz - ts_fuzz) / 1000.0

    // Printing results
    //    stats.failureMap.foreach{case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName,msg.mkString(","))} $c x $msg")}
    println(s"=== RESULTS: BigFuzz $benchmark_name ===")
    //    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    //    println(s"failures: ${stats.failureMap.map{case (_, (_, _, i)) => i+1}.toSeq.sortBy(i => i).mkString(",")}")
    //    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    //    println(s"iterations: ${stats.plotData._1.mkString(",")}")
    println("Coverage progress (AVG):")
    all_stats.map(stat => expand(stat.plotData._1.zip(stat.plotData._2).toList))
      .foldLeft(Array.fill(Config.iterations)(0.0)) { case (a, b) => a.zip(b).map { case (x, y) => x + y } } // sum the vectors
      .map(_ / Config.maxRepeats)
      .zipWithIndex.map { case (cov, i) => (cov, i + 1) }
      //      .groupBy(_._1)
      //      .flatMap{case (cov, iters) => List((iters(0)._2, cov), (iters.last._2, cov))}
      .foldLeft(List((0.0, 0))) { case (list, (cov, i)) =>
        val (covl, _) = list.last
        if (covl == cov)
          list
        else
          list :+ (cov, i)
      }
      .foreach { case (cov, i) => println(s"($i,${limitDP(cov, 2)})") }


    // Printing results

    Global.maxErrorsMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmark_name, msg.mkString(","))} $c x $msg") }
    Global.maxErrorsMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmark_name, msg.mkString(","))} x $c") }
    Global.maxErrorsMap.map { case (msg, (_, c, i)) => (getLineNo(benchmark_name, msg.mkString("\n")), c, i) }
      .groupBy(_._1)
      .map { case (line, list) => (line, list.size) }
      .toList.sortBy(_._1)
      .foreach(println)
    println(s"MAX UNIQUE ERRORS: ${Global.maxErrorsMap.keySet.size}")


    println("Error progress (AVG):")
    all_stats
      .foldLeft(List.fill(Config.iterations)(0.0)) { case (cumsum, stats) =>
        cumsum.zip(stats.cumulativeError).map { case (x, y) => x + y }
      }
      .map(_ / Config.maxRepeats)
      .map(_ / Config.map_err_count(Config.benchmarkName) * 100)
      .zipWithIndex.map { case (cov, i) => (cov, i + 1) }
      //      .groupBy(_._1)
      //      .flatMap{case (cov, iters) => List((iters(0)._2, cov), (iters.last._2, cov))}
      .foldLeft(List((0.0, 0))) { case (list, (cumerrs, i)) =>
        val (cumerrsl, _) = list.last
        if (cumerrsl == cumerrs)
          list
        else
          list :+ (cumerrs, i)
      }
      .foreach { case (cov, i) => println(s"($i,${limitDP(cov, 2)})") }


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

  def getLineNo(filename: String, trace: String): String = {
    val pattern = s"""${filename}.scala:(\\d+)"""
    pattern.r.findFirstIn(trace) match {
      case Some(str) => str.split(':').last
      case _ => "-"
    }
  }

  def limitDP(d: Double, dp: Int): Double = {
    BigDecimal(d).setScale(dp, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def expand(tuples: List[(Int, Double)]): Array[Double] = {
    val max = Config.iterations
    val expanded = (0 until max).map(_ => 0.0).toArray
    tuples.foreach { case (itr, cov) => expanded(itr) = cov }
    var cumulative = expanded(0)
    expanded.map {
      cov =>
        if (cov == 0.0)
          cumulative
        else {
          cumulative = cov
          cov
        }
    }
  }
}
