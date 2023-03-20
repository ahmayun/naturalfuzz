package runners

import fuzzer.{Fuzzer, Global, Program, SymbolicProgram}
import guidance.RIGGuidance
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import runners.RunRIGFuzz.prettify
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import symbolicexecution.SymbolicExecutor
import utils.MiscUtils.toBinaryStringWithLeadingZeros
import utils.{FilterQueries, Pickle, QueriedRDDs, QueryResult, RIGUtils}
import RunRIGFuzzJar.{getLineNo, limitDP}
import java.io.File
import scala.collection.mutable.ListBuffer

object RunRIGFuzzJarFuzzing extends Serializable {

  def main(args: Array[String]): Unit = {

    println("RunRIGFuzzJar called with following args:")
    println(args.mkString("\n"))

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, sparkMaster, pargs, duration, outDir) =
    if(!args.isEmpty) {
      (args(0),
        args(1),
        args.slice(args.length-2, args.length),
        args(2),
        args(3))
    } else {
      ("FlightDistance", "local[*]",
        Array("flights", "airports").map{s => s"seeds/reduced_data/LongFlights/$s"},
        "10",
        "target/rig-output")
    }
    Config.benchmarkName = benchmarkName
    // val Some(pargs) = Config.mapInputFilesRIGReduced.get(benchmarkName)
    val Some(funFaulty) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(funSymEx) = Config.mapFunSymEx.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = s"examples.faulty.$benchmarkName"
//    val Some(funProbeAble) = Config.mapFunProbeAble.gget(benchmarkName)
    val Some(provInfo) = Config.provInfos.get(benchmarkName)
    // ========================================================

    val scoverageOutputDir = s"$outDir/scoverage-results"

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"
    val program = new Program(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFaulty,
      pargs)

    def createSafeFileName(pname: String, pargs: Array[String]): String = {
      s"$pname"
      //s"${pname}_${pargs.map(_.split("/").last).mkString("-")}"
    }

    val qrs = Pickle.load[List[QueryResult]](s"/home/student/pickled/qrs/${createSafeFileName(benchmarkName, pargs)}.pkl")
    val guidance = new RIGGuidance(pargs, schema, duration.toInt, new QueriedRDDs(qrs))
    val (stats, timeStartFuzz, timeEndFuzz) = Fuzzer.Fuzz(program, guidance, outDir)

    // Finalizing
    val coverage = Serializer.deserialize(new File(s"$scoverageOutputDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(scoverageOutputDir)
    val measurements = IOUtils.invoked(measurementFiles)

    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageOutputDir)).write(coverage)

//    val durationProbe = 0.1f // (timeEndProbe - timeStartProbe) / 1000.0
//    val durationFuzz = (timeEndFuzz - timeStartFuzz) / 1000.0
//    val durationTotal = durationProbe + durationFuzz

//    // Printing results
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} x $c") }
    stats.failureMap.map { case (msg, (_, c, i)) => (getLineNo(benchmarkName, msg.mkString("\n")), c, i) }
      .groupBy(_._1)
      .map { case (line, list) => (line, list.size) }
      .toList.sortBy(_._1)
      .foreach(println)

    println(s"=== RESULTS: RIGFuzz $benchmarkName ===")
    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    println(s"iterations: ${stats.plotData._1.mkString(",")}")
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
        s"\tIterations: ${Global.iteration}"
    )
  }

}
