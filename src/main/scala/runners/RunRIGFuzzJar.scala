package runners

import abstraction.BaseRDD
import fuzzer.Fuzzer.writeToFile
import fuzzer.{NewFuzzer, Global, InstrumentedProgram, Program, SymbolicProgram}
import guidance.RIGGuidance
import scoverage.report.ScoverageHtmlWriter
import scoverage.{IOUtils, Serializer}
import utils.{FilterQueries, Pickle, ProvFuzzUtils, QueriedRDDs, QueryResult, RIGUtils, SatRDDs}
import symbolicexecution.{SymbolicExecutor, SymbolicExpression}
import org.apache.spark.rdd.RDD
import org.apache.spark.util.CollectionAccumulator

import java.io.File
import org.apache.spark.{SparkConf, SparkContext}
import runners.RunRIGFuzz.prettify
import utils.MiscUtils.toBinaryStringWithLeadingZeros

import scala.collection.mutable.ListBuffer

object RunRIGFuzzJar extends Serializable {

  def main(args: Array[String]): Unit = {

    println("RunRIGFuzzJar called with following args:")
    println(args.mkString("\n"))

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, mutantName, sparkMaster, pargs, duration, outDir) =
      if (!args.isEmpty) {
        (args(0),
          args(1),
          args(2),
          args.takeRight(args.length - 5),
          args(3),
          args(4))
      } else {
//        val name = "FlightDistance"
//        (name, "local[*]",
//          Array("flights", "airports").map { s => s"seeds/reduced_data/flightdistance/$s" },
//          "10",
//          s"target/rig-output-local/$name")
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
//        val name = "Q3"
//        val _mutantName = "Q3"
//        (name,
//          _mutantName,
//          "local[*]",
//          Array("store_sales", "date_dim", "item").map { s => s"/home/ahmad/Documents/VT/project2/tpcds-datagen/data_csv_no_header/$s" },
//          "20",
//          s"target/rig-output-local/$name")
        val name = "Q1"
        val _mutantName = "Q1"
        (name,
          _mutantName,
          "local[1]",
          Array("store_returns", "date_dim", "store", "customer").map(s => s"/home/ahmad/Documents/VT/project2/tpcds-datagen/data_csv_no_header/$s"),
          "20",
          s"target/rig-output-local/${_mutantName}")
      }
    Config.benchmarkName = benchmarkName
    Config.sparkMaster = sparkMaster
    val Some(funFaulty) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(funSymEx) = Config.mapFunSymEx.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = s"examples.faulty.$benchmarkName"
    // ========================================================

    val scoverageOutputDir = s"$outDir/scoverage-results"

    val benchmarkPath = s"src/main/scala/${benchmarkClass.split('.').mkString("/")}.scala"
    val program = new Program(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funFaulty,
      Array())

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClass,
      benchmarkPath,
      funSymEx,
      pargs :+ sparkMaster
    )

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster(sparkMaster)
        .setAppName(s"RunRIGFuzzJar: symbolic.${benchmarkName}")
//        .set("spark.executor.memory", "8g")
    )
    sc.setLogLevel("ERROR")


    // create an accumulator in the driver and initialize it to an empty list
    val expressionAccumulator = sc.collectionAccumulator[SymbolicExpression]("ExpressionAccumulator")
    Config.expressionAccumulator = expressionAccumulator
//    monitoring.Monitors.setAccumulator(expressionAccumulator)

    // Preprocessing and Fuzzing
    println("Running monitored program")
    val pathExpressions = SymbolicExecutor.execute(symProgram, expressionAccumulator)
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
//    preJoinFill(1) = preJoinFill(1).filter(row => row._1.split(",")(8) == "11")

    printIntermediateRDDs("Pre Join Path Vectors:", preJoinFill, branchConditions)

    val savedJoins = createSavedJoins(preJoinFill, branchConditions)
    println(s"Saved Joins: ${savedJoins.length}")

    if(savedJoins.length > 0) {
      savedJoins
        .head
        ._1
        .take(10)
        .foreach(println)
    }

    val rdds = branchConditions.createSatVectors(preJoinFill.map(_.zipWithIndex), savedJoins.toArray)
      .map { rdd => rdd.map { case ((row, pv), _) => (row, pv) } }

//    rdds(1) = rdds(1).filter(row => row._1.split(",")(8) == "11")
    printIntermediateRDDs("POST Join Path Vectors:", rdds, branchConditions)

    //    val joinTable = List[List[(Int, List[Int])]](
    //      List((0, List(5)), (1, List(0))),
    //      List((0, List(6)), (1, List(0)))
    //    )

    val joinTable = branchConditions.getJoinConditions.map {
      case (ds1, ds2, cols1, cols2) => List((ds1, cols1), (ds2, cols2))
    }


    val vecs = generateList(2 << 30, branchConditions.getCount)
    vecs.foreach(x => println(toBinaryStringWithLeadingZeros(x)))
//    sys.exit()
    val qrs = vecs
      .zip(branchConditions.filterQueries)
      .map {
        case (mask, q) =>
          val qr = rdds.zipWithIndex.map {
            case (rdd, i) =>
              rdd.filter {
                case (row, pv) =>
                  val result = (pv & mask) != 0
//                  if(q.involvesDS(i)) {
//                    println(s"${toBinaryStringWithLeadingZeros(pv)} = $row")
//                    println(s"${toBinaryStringWithLeadingZeros(mask)} = MASK")
//                    println(s"$result = RESULT")
//                  }
                  result
              }
                .map {
                  case (row, pv) =>
                    s"$row${Config.delimiter}$pv"
                }
                .takeSample(false, 10).toSeq
          }
          new QueryResult(qr, Seq(q), q.locs)
      }

    qrs.foreach {
      qr =>
        println(s"qr: ${qr.query(0).tree}------")
        qr.filterQueryRDDs.foreach(rdd => rdd.foreach(println))
    }


    // get the maximum number of keys extracted from a row
    // this is how many duplicate rows will be allowed (duplicate w.r.t branch vector)
    val maxKeysFromRow = 2
    //    sys.exit(0)
    val reducedDatasets = ListBuffer[List[(String, Long)]]()
    val pvs = ListBuffer[Int]()
    rdds
      .zipWithIndex
      .foreach {
        case (rdd, d) =>
          val (red, cumuPV, _) = rdd
            .zipWithIndex
            .aggregate(
              (List[(String, Long)](), 0x0, 0))({
              // min rows max bit fill algorithm here
              // use join table to guide selection according to rdd1 selection
              case ((acc, accVec, selected), ((row, pv), rowi)) =>
                val or = accVec | pv
                if (or != accVec && (checkMembership((row, d, rowi), reducedDatasets, joinTable) || joinTable.isEmpty)) { // Note: section can be optimized with areNewBitsAfterJoin()
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
          pvs.append(cumuPV)
      }

    println("JOIN TABLE")
    joinTable.foreach(println)

    reducedDatasets
      .zip(pvs)
      .zipWithIndex
      .foreach {
        case ((ds, pv), i) =>
          println(s"==== Reduced DS: ${i} ${toBinaryStringWithLeadingZeros(pv)}=====")
          ds.foreach(println)
          println("-----")
      }

    val finalReduced = reducedDatasets.map {
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
    Pickle.dump(qrs, s"pickled/$foldername.pkl")
    val dataset_files = finalReduced.zipWithIndex.map { case (e, i) => writeToFile(s"./seeds/rig_reduced_data/$foldername", e, i) }
    val qrsLoaded = Pickle.load[List[QueryResult]](s"pickled/${createSafeFileName(benchmarkName, pargs)}.pkl")
    val guidance = new RIGGuidance(dataset_files, schema, duration.toInt, new QueriedRDDs(qrsLoaded))
    //    Fuzzer.Fuzz(program, guidance, outDir)

    //    val satRDDs = runnablePieces.createSatVectors(program.args) // create RDD with bit vector and bit counts
    //    val minSatRDDs = satRDDs.getRandMinimumSatSet()
    //    val brokenRDDs: List[QueryResult] = minSatRDDs.breakIntoQueryRDDs() // not ideal, but allows me to leverage my own existing code
    //
    //
    //    val guidance = new RIGGuidance(inputFiles, schema, runs, new QueriedRDDs(brokenRDDs))
    //
    val (stats, timeStartFuzz, timeEndFuzz) = NewFuzzer.FuzzMutants(program, program, guidance, outDir)

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
    println("DETECTED JOINS")
    joins.foreach(println)

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
                  val key = colsB.map(c => try{cols(c)} catch {case _ : Throwable => "null"}).mkString("|")
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
