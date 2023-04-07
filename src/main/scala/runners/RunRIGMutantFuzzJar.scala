package runners

import fuzzer.{NewFuzzer, Global, Program, SymbolicProgram, DynLoadedProgram}
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

object RunRIGMutantFuzzJar extends Serializable {

  def main(args: Array[String]): Unit = {

    println("RunRIGMutantFuzzJar called with following args:")
    println(args.mkString("\n"))

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, mutantName, sparkMaster, pargs, duration, outDir) =
      if (!args.isEmpty) {
        (args(0),
          args(1),
          args(2),
          if (args.length == 7) args.takeRight(2) else args.takeRight(1),
          args(3),
          args(4))
      } else {
        val name = "FlightDistance"
        val _mutantName = "FlightDistance_M9_42_times_div"
        (name,
          _mutantName,
          "local[*]",
          Array("dataset_0", "dataset_1").map { s => s"seeds/rig_reduced_data/$name/$s" },
          "20",
          s"target/rig-output-local/${_mutantName}")
//        val name = "WebpageSegmentation"
//        val _mutantName = "WebpageSegmentation_M19_83_lte_neq"
//        (name,
//          _mutantName,
//          "local[1]",
//          Array("dataset_0", "dataset_1").map { s => s"./seeds/rig_reduced_data/$name/$s" },
//          "20",
//          s"target/rig-output-local/${_mutantName}")
//        val name = "Delays"
//        (name, "local[*]",
//          Array("station1", "station2").map { s => s"seeds/reduced_data/delays/$s" },
//          "30",
//          s"target/rig-output-local/$name")
      }
    Config.benchmarkName = benchmarkName
    Config.sparkMaster = sparkMaster
    val Some(funFaulty) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = s"examples.faulty.$benchmarkName"
    val mutantClass = s"examples.mutants.$benchmarkName.$mutantName"
    // ========================================================

    val scoverageOutputDir = s"$outDir/scoverage-results"

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"
    val mutantPath = s"src/main/scala/${mutantClass.split('.').mkString("/")}.scala"
    val Some(funMutant) = Config.mapFunMutants.get(mutantName) // getMainFunctionDynamically(mutantClass)
    val program = new Program(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFaulty,
      pargs)

    val mutantProgram = new Program(
      mutantName,
      mutantClass,
      mutantPath,
      funMutant,
      pargs)

//    val mutantProgram = new DynLoadedProgram(
//      mutantName,
//      mutantClass,
//      mutantPath,
//      funMutant,
//      pargs)

    def createSafeFileName(pname: String, pargs: Array[String]): String = {
      s"$pname"
      //s"${pname}_${pargs.map(_.split("/").last).mkString("-")}"
    }


    val qrs = Pickle.load[List[QueryResult]](s"pickled/${createSafeFileName(benchmarkName, pargs)}.pkl")
    val guidance = new RIGGuidance(pargs, schema, duration.toInt, new QueriedRDDs(qrs))

    val (stats, timeStartFuzz, timeEndFuzz) = NewFuzzer.FuzzMutants(program, mutantProgram, guidance, outDir)

    // Finalizing
//    val coverage = Serializer.deserialize(new File(s"$scoverageOutputDir/scoverage.coverage"))
//    val measurementFiles = IOUtils.findMeasurementFiles(scoverageOutputDir)
//    val measurements = IOUtils.invoked(measurementFiles)
//
//    coverage.apply(measurements)
//    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageOutputDir)).write(coverage)

    //    val durationProbe = 0.1f // (timeEndProbe - timeStartProbe) / 1000.0
    //    val durationFuzz = (timeEndFuzz - timeStartFuzz) / 1000.0
    //    val durationTotal = durationProbe + durationFuzz


    // Printing results
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
//    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")
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

  def getMainFunctionDynamically(fullClassName: String): java.lang.reflect.Method = {
    val runtimeClass = Class.forName(fullClassName)
    runtimeClass.getMethod("main", classOf[Array[String]])
  }

  def generateList(start: Int, count: Int): List[Int] = {
    require(count >= 1, "Invalid value for count")
    if (count == 1) {
      List(start)
    } else {
      start :: generateList(start >>> 2, count - 1)
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
            found = found || {
              val res = hashKeys(thisRow, thisCols) == hashKeys(otherRow, otherCols)
              res
            }
          }
        }
    }
    found
  }

  def hashKeys(row: Array[String], cols: List[Int]): Int = {
    // hash(thisRow(thisCols.head)) == hash(otherRow(otherCols.head))
    val (selected, _) = row
      .zipWithIndex
      .filter {
        case (e, i) =>
          cols.contains(i)
      }
      .unzip

    val res = selected
      .sorted
      .toList // required to produce reliable hashcode for a list with same elements
      .hashCode

    //    println(s"HASHING [$res]: ${cols.mkString("|")} - ${selected.mkString(",")}")
    return res
  }


  def checkMembership(rowInfo: (String, Int, Long), reduced: ListBuffer[List[(String, Long)]], joinTable: List[List[(Int, List[Int])]]): Boolean = {
    if (reduced.isEmpty)
      return true

    if (rowInfo._2 == 0)
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
          ), dsA, dsB)
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
