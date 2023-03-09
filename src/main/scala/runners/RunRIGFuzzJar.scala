package runners

import fuzzer.{Fuzzer, Global, InstrumentedProgram, Program, SymbolicProgram}
import guidance.RIGGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import utils.{FilterQueries, ProvFuzzUtils, RIGUtils}
import symbolicexecution.SymbolicExecutor
import org.apache.spark.rdd.RDD
import java.io.File
import org.apache.spark.{SparkConf, SparkContext}
import runners.RunRIGFuzz.prettify
import utils.MiscUtils.toBinaryStringWithLeadingZeros


object RunRIGFuzzJar {

  def main(args: Array[String]): Unit = {

    println("RunRIGFuzzJar called with following args:")
    println(args.mkString("\n"))

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, sparkMaster, pargs) =
    if(!args.isEmpty) {
      (args(0), args(1), args.slice(args.length-2, args.length))
    } else {
      ("FlightDistance", "local[*]", Array("flights", "airports").map{s => s"seeds/reduced_data/LongFlights/$s"})
    }
    val duration = "10" // args(2)
    val outDir = "/dev/null" // args(3)
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

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funSymEx,
      pargs:+sparkMaster)

    // Preprocessing and Fuzzing
    println("Running monitored program")
    val pathExpressions = SymbolicExecutor.execute(symProgram)
    println("Creating filter queries")
    val branchConditions = RIGUtils.createFilterQueries(pathExpressions)
    println("All pieces:")
    branchConditions
      .filterQueries
      .zipWithIndex
      .foreach {
        case (q, i) =>
          println(i, q.tree)
      }

    val sc = SparkContext.getOrCreate(new SparkConf())
    sc.setLogLevel("ERROR")
    val rawDS = pargs
      .map(sc.textFile(_))

    val preJoinFill = branchConditions.createSatVectors(rawDS)

    printIntermediateRDDs("Pre Join Path Vectors:", preJoinFill, branchConditions)

    val savedJoins = createSavedJoins(preJoinFill, branchConditions)
    println("Saved Joins")
    savedJoins
      .head
      ._1
      .take(10)
      .foreach(println)

    val rdds = branchConditions.createSatVectors(preJoinFill.map(_.zipWithIndex()), savedJoins.toArray)
      .map{rdd => rdd.map{ case ((row, pv), _) => (row, pv)}}

    printIntermediateRDDs("POST Join Path Vectors:", rdds, branchConditions)



//    val satRDDs = runnablePieces.createSatVectors(program.args) // create RDD with bit vector and bit counts
//    val minSatRDDs = satRDDs.getRandMinimumSatSet()
//    val brokenRDDs: List[QueryResult] = minSatRDDs.breakIntoQueryRDDs() // not ideal, but allows me to leverage my own existing code
//
//
//    val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(brokenRDDs))
//
//    val (stats, timeStartFuzz, timeEndFuzz) = Fuzzer.Fuzz(program, guidance, outDir)
//
//    // Finalizing
//    val coverage = Serializer.deserialize(new File(s"$scoverageOutputDir/scoverage.coverage"))
//    val measurementFiles = IOUtils.findMeasurementFiles(scoverageOutputDir)
//    val measurements = IOUtils.invoked(measurementFiles)
//
//    coverage.apply(measurements)
//    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageOutputDir)).write(coverage)
//
//    val durationProbe = 0.1f // (timeEndProbe - timeStartProbe) / 1000.0
//    val durationFuzz = (timeEndFuzz - timeStartFuzz) / 1000.0
//    val durationTotal = durationProbe + durationFuzz
//
//
//    // Printing results
//    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} $c x $msg") }
//    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} x $c") }
//    stats.failureMap.map { case (msg, (_, c, i)) => (getLineNo(benchmarkName, msg.mkString("\n")), c, i) }
//      .groupBy(_._1)
//      .map { case (line, list) => (line, list.size) }
//      .toList.sortBy(_._1)
//      .foreach(println)
//
//    println(s"=== RESULTS: RIGFuzz $benchmarkName ===")
//    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
//    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
//    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
//    println(s"iterations: ${stats.plotData._1.mkString(",")}")
//    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
//    println(s"Total Time (s): ${limitDP(durationTotal, 2)} (P: $durationProbe | F: $durationFuzz)")
//    println(
//      s"Config:\n" +
//        s"\tProgram: ${program.name}\n" +
//        s"\tMutation Distribution M1-M6: ${guidance.get_mutate_probs.mkString(",")}\n" +
//        s"\tActual Application: ${guidance.get_actual_app.mkString(",")}\n" +
//        s"\tIterations: ${Global.iteration}"
//    )
//    println("ProvInfo: ")
//    println(provInfo)
  }

  def limitDP(d: Double, dp: Int): Double = {
    BigDecimal(d).setScale(dp, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def getLineNo(filename: String, trace: String): String = {
    val pattern = s"""$filename.scala:(\\d+)"""
    pattern.r.findFirstIn(trace) match {
      case Some(str) => str.split(':').last
      case _ => "-"
    }
  }

  def createSavedJoins(preJoinFilled: Array[RDD[(String, Int)]], branchConditions: FilterQueries): List[(RDD[(String, ((String, Long), (String, Long)))], Int, Int)] = {
    val joins = branchConditions.getJoinConditions // returns (ds0ID,ds1ID,List(colsDs0),List(colsDs1))
    joins.map {
      case (dsA, dsB, colsA, colsB) =>
        (preJoinFilled(dsA)
          .zipWithIndex
          .map {
            case ((row, _), i) =>
              val cols = row.split(Config.delimiter)
              val key = colsA.map(c => cols(c)).mkString("|")
              (key, (row, i))
          }
        .join(
          preJoinFilled(dsB)
            .zipWithIndex
            .map {
              case ((row, _), i) =>
                val cols = row.split(Config.delimiter)
                val key = colsB.map(c => cols(c)).mkString("|")
                (key, (row, i))
            }
        ),dsA,dsB)
    }
  }

  def printIntermediateRDDs(heading: String, rdds: Array[RDD[(String, Int)]], branchConditions: FilterQueries): Unit = {
    println(heading)
    rdds
      .zipWithIndex
      .foreach {
        case (rdd, i) =>
          println(s"RDD $i:")
          println(s"|\tds_row\t\t\t\t|\t${branchConditions.filterQueries.map(_.tree).zipWithIndex.mkString("", "\t|\t", "\t|")}")
          rdd
            .take(10)
            .foreach {
              case (row, pv) =>
                println(prettify(row, toBinaryStringWithLeadingZeros(pv).take(branchConditions.filterQueries.length * 2), branchConditions))
            }
      }
  }
}
