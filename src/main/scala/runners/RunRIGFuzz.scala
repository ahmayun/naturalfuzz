package runners

import fuzzer.{Fuzzer, InstrumentedProgram, Program, ProvInfo, SymbolicProgram}
import org.apache.spark.{SPARK_BRANCH, SparkConf, SparkContext}
import symbolicexecution.SymbolicExecutor
import utils.{FilterQueries, QueriedRDDs, QueryResult, RIGUtils}
import guidance.RIGGuidance
import runners.RunNormal.limitDP
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import utils.MiscUtils.toBinaryStringWithLeadingZeros
import utils.TimingUtils.timeFunction
import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object RunRIGFuzz extends Serializable {


  def prettify(row: String, pathVector: String, pieces: FilterQueries): String = {
    val broken = pathVector.foldLeft(List[String]("")) {
      case (acc, e) =>
        if (acc.last.length < 2) {
          acc.init :+ (acc.last + e)
        } else {
          acc ++ List(e.toString)
        }
    }
      s"|\t$row\t|\t\t${broken.mkString("", "\t\t\t\t|\t\t\t\t", "\t\t\t\t|")}"
  }

  def main(args: Array[String]): Unit = {

    val runs = Config.fuzzDuration

    // ==P.U.T. dependent configurations=======================
    val benchmarkName = Config.benchmarkName
    val Some(inputFiles) = Config.mapInputFiles.get(benchmarkName)
    val Some(funFuzzable) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(funSymEx) = Config.mapFunSymEx.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = Config.benchmarkClass
    // ========================================================

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"

    val outputDir = s"${Config.resultsDir}/RIGFuzz"
    val scoverageResultsDir = s"$outputDir/scoverage-results"

    //    val klass = Class.forName(s"examples.clustermonitor.$benchmarkName")
    //    val mainFunc = klass.getMethod("main", Array[String]().getClass)
    //    val clusterProgram = new InstrumentedProgram(
    //      benchmarkName,
    //      s"examples.clustermonitor.$benchmarkName",
    //      s"src/main/scala/examples/clustermonitor/$benchmarkName.scala",
    //      (args: Array[String]) => mainFunc.invoke(klass, args).asInstanceOf[ProvInfo],
    //      inputFiles)
    //    val provInfo = clusterProgram.main(Array("mixmatch-data/rig-test/boxes", "local[*]"))
    //    println(provInfo)
    //    sys.exit(0)

    val program = new Program(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFuzzable,
      inputFiles)

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funSymEx,
      inputFiles)

    // Preprocessing and Fuzzing
    val pathExpressions = SymbolicExecutor.execute(symProgram)
    val runnablePieces = RIGUtils.createFilterQueries(pathExpressions)
    println("All pieces:")
    runnablePieces
      .filterQueries
      .zipWithIndex
      .foreach {
        case (q, i) =>
          println(i, q.tree)
      }

    val sc = SparkContext.getOrCreate(new SparkConf())
    sc.setLogLevel("ERROR")

    val rawDS = inputFiles
      .map(sc.textFile(_))

    val preJoinFill = runnablePieces.createSatVectors(rawDS)

    println("Pre Join Path Vectors:")
    preJoinFill
      .zipWithIndex
      .foreach {
        case (rdd, i) =>
          println(s"RDD $i:")
          rdd
            .collect()
            .foreach {
              case (row, pv) =>
                println(row, toBinaryStringWithLeadingZeros(pv).take(runnablePieces.filterQueries.length * 2))
            }
      }

    //    val datasets = rawDS.map(_.zipWithIndex())

    val savedJoins = Array( // TODO: Generalize hardcoded saved join
      (preJoinFill
        .head
        .zipWithIndex
        .map {
          case ((row, _), i) =>
            val key = row.split(",").head
            (key, (row, i))
        }
        .join(
          preJoinFill
            .last
            .zipWithIndex
            .map {
              case ((row, _), i) =>
                val key = row.split(",").head
                (key, (row, i))
            })
        , 0, 1))

    println("Saved Joins")
    savedJoins
      .head
      ._1
      .collect()
      .foreach(println)

    // compute sat vectors for RDDs
    val rdds = runnablePieces.createSatVectors(preJoinFill.map(_.zipWithIndex()), savedJoins)

    rdds.foreach {
      case rdd =>
        rdd
          .collect()
          .foreach {
            case (row, _) => println(row)
          }
    }

    // Assume join lookup tables are ready


    val joinTable = List[List[(Int, List[Int])]](
      List((0, List(0)), (1, List(0)))
    )
    val reducedDatasets = ListBuffer[List[(String, Long)]]()

    println("All Pieces:")
    runnablePieces
      .filterQueries
      .zipWithIndex
      .foreach {
        case (q, i) =>
          println(i, q.tree)
      }

    println("RDDs")
    rdds.zipWithIndex.foreach {
      case (rdd, i) =>
        println(s"\nRDD $i")
        println(s"|\tds_row\t\t\t\t|\t${runnablePieces.filterQueries.map(_.tree).mkString("", "\t|\t", "\t|")}")
        rdd
          .collect()
          .foreach {
            case ((row, pv), rowi) =>
              println(prettify(row, toBinaryStringWithLeadingZeros(pv).take(runnablePieces.filterQueries.length * 2), runnablePieces))
          }
    }

    rdds
      .zipWithIndex
      .foreach {
        case (rdd, d) =>
          val (red, _) = rdd.aggregate(
            (List[(String, Long)](), 0x0))({
            // min rows max bit fill algorithm here
            // use join table to guide selection according to rdd1 selection
            case ((acc, accVec), ((row, pv), rowi)) =>
              val or = accVec | pv
              if (or != accVec && checkMembership((row, d, rowi), reducedDatasets)) { // Note: section can be optimized with areNewBitsAfterJoin()
                (acc :+ (row, rowi), or)
              } else {
                (acc, accVec)
              }
          }, {
            case ((acc1, accVec1), (acc2, accVec2)) =>
              val accVec = accVec1 | accVec2
              if (accVec == accVec1 && accVec == accVec2) {
                (acc1, accVec)
              } else if (accVec == accVec1 && accVec != accVec2) {
                (acc1, accVec1)
              } else if (accVec != accVec1 && accVec == accVec2) {
                (acc2, accVec2)
              } else {
                (acc1 ++ acc2, accVec)
              }
          })
          reducedDatasets.append(red)
      }

    reducedDatasets.zipWithIndex.foreach {
      case (ds, i) =>
        println(s"==== Reduced DS: ${i + 1} =====")
        ds.foreach(println)
        println("-----")
    }

    def checkMembership(rowInfo: (String, Int, Long), reduced: ListBuffer[List[(String, Long)]]): Boolean = {
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

    def hash(s: String): Int = s.hashCode


    //    val satRDDs = runnablePieces.createSatVectors(program.args) // create RDD with bit vector and bit counts
    //    val minSatRDDs = satRDDs.getRandMinimumSatSet()
    //    val brokenRDDs: List[QueryResult] = minSatRDDs.breakIntoQueryRDDs() // not ideal, but allows me to leverage my own existing code


    //    val (queryRDDs, _) = timeFunction(() => filterQueries.getRows(program.args))
//        val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(brokenRDDs))
//        val (stats, _, _) = Fuzzer.Fuzz(program, guidance, outputDir)


    //    val coverage = Serializer.deserialize(new File(s"$scoverageResultsDir/scoverage.coverage"))
    //    val measurementFiles = IOUtils.findMeasurementFiles(scoverageResultsDir)
    //    val measurements = IOUtils.invoked(measurementFiles)

    //    coverage.apply(measurements)
    //    new ScoverageHtmlWriter(Seq(new File("src/main/scala")), new File(scoverageResultsDir)).write(coverage)
    //    println("=== qrdds ====")
    //    brokenRDDs.foreach{
    //      q =>
    //        println("===")
    //        println(q.toString)
    //    }
    //    println(s"Coverage: ${limitDP(coverage.statementCoveragePercent, 2)}% (gathered from ${measurementFiles.length} measurement files)")

  }

}
