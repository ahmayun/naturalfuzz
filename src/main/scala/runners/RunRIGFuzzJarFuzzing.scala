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
    val guidance = new RIGGuidance(pargs, schema, 10, new QueriedRDDs(qrs))
//    Fuzzer.Fuzz(program, guidance, outDir)

//    val satRDDs = runnablePieces.createSatVectors(program.args) // create RDD with bit vector and bit counts
//    val minSatRDDs = satRDDs.getRandMinimumSatSet()
//    val brokenRDDs: List[QueryResult] = minSatRDDs.breakIntoQueryRDDs() // not ideal, but allows me to leverage my own existing code
//
//
//    val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(brokenRDDs))
//
    val (stats, timeStartFuzz, timeEndFuzz) = Fuzzer.Fuzz(program, guidance, outDir)
//
//    // Finalizing
    val coverage = Serializer.deserialize(new File(s"$scoverageOutputDir/scoverage.coverage"))
    val measurementFiles = IOUtils.findMeasurementFiles(scoverageOutputDir)
    val measurements = IOUtils.invoked(measurementFiles)
//
    coverage.apply(measurements)
    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageOutputDir)).write(coverage)

//    val durationProbe = 0.1f // (timeEndProbe - timeStartProbe) / 1000.0
//    val durationFuzz = (timeEndFuzz - timeStartFuzz) / 1000.0
//    val durationTotal = durationProbe + durationFuzz

//
//    // Printing results
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(benchmarkName, msg.mkString(","))} x $c") }
    stats.failureMap.map { case (msg, (_, c, i)) => (getLineNo(benchmarkName, msg.mkString("\n")), c, i) }
      .groupBy(_._1)
      .map { case (line, list) => (line, list.size) }
      .toList.sortBy(_._1)
      .foreach(println)
//
    println(s"=== RESULTS: RIGFuzz $benchmarkName ===")
    println(s"Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    println(s"iterations: ${stats.plotData._1.mkString(",")}")
    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
//    println(s"Total Time (s): ${limitDP(durationTotal, 2)} (P: $durationProbe | F: $durationFuzz)")
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
//        s"\tMutation Distribution M1-M6: ${guidance.get_mutate_probs.mkString(",")}\n" +
//        s"\tActual Application: ${guidance.get_actual_app.mkString(",")}\n" +
        s"\tIterations: ${Global.iteration}"
    )
//    println("ProvInfo: ")
//    println(provInfo)
  }

  def generateList(start: Int, count: Int): List[Int] = {
    require(count >= 1, "Invalid value for count")
    if (count == 1) {
      List(start)
    } else {
      start :: generateList(start >>> 2, count-1)
    }
  }

  def computeHashEquivalence(rowInfo: (String, Int, Long), reducedDSRow: String, reducedDSID: Int, joinTable: List[List[(Int, List[Int])]]): Boolean = {
    val (row, ds, rowID) = rowInfo
    var found = false
    joinTable.foreach {
      case List((ds1, cols1), (ds2, cols2)) =>
        if (ds1 == ds || ds2 == ds) {
          val (otherDS, otherCols, thisDS, thisCols) = if (ds1 == ds) (ds2, cols2, ds, cols1) else (ds1, cols1, ds2, cols2)
          if (otherDS == reducedDSID) {
            val thisRow = row.split(",")
            val otherRow = reducedDSRow.split(",")
            found = found || hash(thisRow(thisCols.head)) == hash(otherRow(otherCols.head)) // TODO: Generalize this to compound keys
          }
        }
    }
    found
  }

  def checkMembership(rowInfo: (String, Int, Long), reduced: ListBuffer[List[(String, Long)]], joinTable: List[List[(Int, List[Int])]]): Boolean = {
    if (reduced.isEmpty)
      return true

    val (rddRow, dsID, rowID) = rowInfo
    var member = false
    reduced
      .zipWithIndex
      .foreach {
        case (ds, i) =>
          ds.foreach {
            case (reducedDSRow, _) =>
              member ||= computeHashEquivalence(rowInfo, reducedDSRow, i, joinTable)
          }
      }
    member
  }

  def hash(s: String): Int = s.hashCode
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
          println(s"|\tds_row\t\t\t\t|\t${branchConditions.filterQueries.map(_.tree).mkString("", "\t|\t", "\t|")}")
          rdd
            .take(10)
            .foreach {
              case (row, pv) =>
                println(prettify(row, toBinaryStringWithLeadingZeros(pv).take(branchConditions.filterQueries.length * 2), branchConditions))
            }
      }
  }
}
