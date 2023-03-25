package runners
import fuzzer.Fuzzer.writeToFile

import java.net.InetAddress
import fuzzer.{Fuzzer, Global, Program, SymbolicProgram}
import guidance.RIGGuidance
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import runners.RunRIGFuzz.prettify
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import symbolicexecution.{SymbolicExecutor, SymbolicExpression}
import utils.MiscUtils.toBinaryStringWithLeadingZeros
import utils.{FilterQueries, Pickle, QueriedRDDs, QueryResult, RIGUtils}
import RunRIGFuzzJar.{generateList, createSavedJoins, checkMembership, printIntermediateRDDs}
import org.apache.spark.util.AccumulatorV2
import java.io.File
import scala.collection.mutable.ListBuffer

object RunRIGFuzzJarCluster extends Serializable {

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

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funSymEx,
      pargs:+sparkMaster)

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster(sparkMaster)
        .setAppName(s"RunRIGFuzzJar: symbolic.${benchmarkName}")
    )
    sc.setLogLevel("ERROR")

    // create an accumulator in the driver and initialize it to an empty list
    val expressionAccumulator = sc.collectionAccumulator[SymbolicExpression]("ExpressionAccumulator")
    monitoring.Monitors.setAccumulator(expressionAccumulator)

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

//    val joinTable = List[List[(Int, List[Int])]](
//      List((0, List(5)), (1, List(0))),
//      List((0, List(6)), (1, List(0))),
//    )

    val joinTable = branchConditions.getJoinConditions.map {
      case (ds1, ds2, cols1, cols2) => List((ds1, cols1), (ds2, cols2))
    }
    val maxKeysFromRow = 2
    //    sys.exit(0)
    val reducedDatasets = ListBuffer[List[(String, Long)]]()
    rdds
      .zipWithIndex
      .foreach {
        case (rdd, d) =>
          val (red, _, _) = rdd
            .zipWithIndex
            .aggregate(
              (List[(String, Long)](), 0x0, 0))({
              // min rows max bit fill algorithm here
              // use join table to guide selection according to rdd1 selection
              case ((acc, accVec, selected), ((row, pv), rowi)) =>
                val or = accVec | pv
                if (or != accVec && checkMembership((row, d, rowi), reducedDatasets, joinTable)) { // Note: section can be optimized with areNewBitsAfterJoin()
                  (acc :+ (row, rowi), or, selected + 1)
                }
                else if (or == accVec && selected < maxKeysFromRow && checkMembership((row, d, rowi), reducedDatasets, joinTable)) {
                  (acc :+ (row, rowi), or, selected + 1)
                } else {
                  (acc, accVec, selected)
                }
            }, {
              case ((acc1, accVec1, _), (acc2, accVec2, _)) =>
                val accVec = accVec1 | accVec2
                if (accVec == accVec1 && accVec == accVec2) {
                  (acc1, accVec, 0)
                } else if (accVec == accVec1 && accVec != accVec2) {
                  (acc1, accVec1, 0)
                } else if (accVec != accVec1 && accVec == accVec2) {
                  (acc2, accVec2, 0)
                } else {
                  (acc1 ++ acc2, accVec, 0)
                }
            })
          reducedDatasets.append(red)
      }

    reducedDatasets
      .zipWithIndex
      .foreach {
      case (ds, i) =>
        println(s"==== Reduced DS: ${i + 1} =====")
        ds.foreach(println)
        println("-----")
    }

//    val blendedRows = rdds.map(rdd => rdd.map{case (row, pv) => s"$row${Config.delimiter}$pv"}.collect().toSeq)
//    val minRDDs = new SatRDDs(blendedRows, branchConditions).getRandMinimumSatSet()

    val qrs = generateList(3 << 30, branchConditions.getCount).zip(branchConditions.filterQueries).map{
      case (mask,q) =>
        val qr = rdds.map {
          rdd =>
            rdd.filter {
              case (row, pv) =>
                (pv & mask) != 0
            }
              .map{case (row, pv) => s"$row${Config.delimiter}$pv"}
              .takeSample(false, 10).toSeq
        }
        new QueryResult(qr,Seq(q),q.locs)

    }

    qrs.foreach {
      qr =>
        println("qr------")
        qr.filterQueryRDDs.foreach(rdd => rdd.foreach(println))
    }

    val finalReduced = reducedDatasets.map{
      rdd =>
        rdd.map {
          case (row, i) => row
        }.toSeq
    }.toArray

    def createSafeFileName(pname: String, pargs: Array[String]): String = {
      s"$pname"
      //s"${pname}_${pargs.map(_.split("/").last).mkString("-")}"
    }
    val foldername = createSafeFileName(benchmarkName, pargs)
    Pickle.dump(qrs, s"/home/student/pickled/qrs/$foldername.pkl")
    finalReduced.zipWithIndex.map{case (e, i) => writeToFile(s"/home/student/pickled/reduced_data/$foldername", e, i)}
  }


}
