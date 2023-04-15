package runners

import fuzzer.Fuzzer.writeToFile
import fuzzer.{Program, SymbolicProgram}
import org.apache.spark.{SparkConf, SparkContext}
import runners.RunRIGFuzzJar.{checkMembership, createSavedJoins, generateList, printIntermediateRDDs}
import symbolicexecution.{SymbolicExecutor, SymbolicExpression}
import utils.MiscUtils.toBinaryStringWithLeadingZeros
import utils.{Pickle, QueryResult, RIGUtils}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scoverage.Platform.FileWriter
import scala.collection.mutable.ListBuffer
import java.io.File

object RunRIGFuzzOverheadTest extends Serializable {

  def main(args: Array[String]): Unit = {

    println("RIGFuzzOverheadTest called with following args:")
    println(args.mkString("\n"))

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, sparkMaster, pargs, overheadOutDir) =
      if (!args.isEmpty) {
        (args(0),
          args(1),
          args.takeRight(args.length - 3), args(2))
      } else {
        val name = "Q3"
        (name, "local[*]",
          Array("0", "1", "2").map { s => s"seeds/rig_reduced_data/$name/dataset_$s" }, "./overhead")
      }
    Config.benchmarkName = benchmarkName
    val Some(funSymEx) = Config.mapFunSymEx.get(benchmarkName)
    val Some(funOrig) = Config.mapFunSpark.get(benchmarkName)
    val benchmarkClassSymEx = s"examples.symbolic.$benchmarkName"
    val benchmarkPathSymEx = s"src/main/scala/${benchmarkClassSymEx.split('.').mkString("/")}.scala"
    val benchmarkClassOrig = s"examples.tpcds.$benchmarkName"
    val benchmarkPathOrig = s"src/main/scala/${benchmarkClassOrig.split('.').mkString("/")}.scala"
    // ========================================================

    val foldername = createTimeStampedName(s"$benchmarkName", overheadOutDir)
    new File(foldername).mkdirs()

    val symProgram = new SymbolicProgram(
      benchmarkName,
      benchmarkClassSymEx,
      benchmarkPathSymEx,
      funSymEx,
      pargs :+ sparkMaster)

    val origProgram = new Program(
      benchmarkName,
      benchmarkClassOrig,
      benchmarkPathSymEx,
      funOrig,
      pargs :+ sparkMaster)

    val sc = SparkContext.getOrCreate(
      new SparkConf()
        .setMaster(sparkMaster)
        .setAppName(s"Overhead Test: ${benchmarkName}")
    )
    sc.setLogLevel("ERROR")

    val t_start_orig = System.currentTimeMillis()
    origProgram.invokeMain(origProgram.args)
    val t_end_orig = System.currentTimeMillis()
    val durationOrig = t_end_orig - t_start_orig
    writeStringToFile(s"Original Runtime(ms) : ${durationOrig}", s"$foldername/runtime")

    // start timing
    val t_start_symex = System.currentTimeMillis()
    val expressionAccumulator = sc.collectionAccumulator[SymbolicExpression]("ExpressionAccumulator")
    println("Running monitored program")
    val pathExpressions = SymbolicExecutor.execute(symProgram, expressionAccumulator)
    val t_end_symex = System.currentTimeMillis()
    val durationSym = t_end_symex - t_start_symex
    writeStringToFile(s"Instrumented Runtime(ms) : ${durationSym}", s"$foldername/runtime", true)

    val t_start_post = System.currentTimeMillis()
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
    if (savedJoins.length > 0) {
      savedJoins
        .head
        ._1
        .take(10)
        .foreach(println)
    }

    val rdds = branchConditions.createSatVectors(preJoinFill.map(_.zipWithIndex()), savedJoins.toArray)
      .map { rdd => rdd.map { case ((row, pv), _) => (row, pv) } }

    printIntermediateRDDs("POST Join Path Vectors:", rdds, branchConditions)

    //    val joinTable = List[List[(Int, List[Int])]](
    //      List((0, List(5)), (1, List(0))),
    //      List((0, List(6)), (1, List(0))),
    //    )

    val joinTable = branchConditions.getJoinConditions.map {
      case (ds1, ds2, cols1, cols2) => List((ds1, cols1), (ds2, cols2))
    }

    val vecs = generateList(2 << 30, branchConditions.getCount)
    vecs.foreach(x => println(toBinaryStringWithLeadingZeros(x)))
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

    qrs.foreach {
      qr =>
        println("qr------")
        qr.filterQueryRDDs.foreach(rdd => rdd.foreach(println))
    }

    println("JOIN TABLE")
    joinTable.foreach(println)

    reducedDatasets
      .zipWithIndex
      .foreach {
        case (ds, i) =>
          println(s"==== Reduced DS: ${i + 1} =====")
          ds.foreach(println)
          println("-----")
      }

    val finalReduced = reducedDatasets.map {
      rdd =>
        rdd.map {
          case (row, i) => row
        }.toSeq
    }.toArray


    Pickle.dump(qrs, s"$foldername/qrs.pkl")
    finalReduced.zipWithIndex.map { case (e, i) => writeToFile(s"$foldername/reduced_data", e, i) }
    val t_end_post = System.currentTimeMillis()
    val durationPost = t_end_post - t_start_post

    writeStringToFile(s"Post Processing (ms) : ${durationPost}", s"$foldername/runtime", true)
    writeStringToFile(s"Overhead w/o post : ${durationSym.toFloat/durationOrig.toFloat}", s"$foldername/runtime", true)
    writeStringToFile(s"Overhead w/ post : ${(durationSym+durationPost).toFloat/durationOrig}", s"$foldername/runtime", true)

  }

  def createTimeStampedName(pname: String, dir: String): String = {
    val date = LocalDateTime.now()
    val format = "'day'dd-HH'hr'-mm'm'-ss's'"
    val formatter = DateTimeFormatter.ofPattern(format)
    val time_string = date.format(formatter)
    s"$dir/${pname}_$time_string"
  }


  def writeStringToFile(s: String, file: String, append: Boolean = false): Unit = {
    val writer = new FileWriter(new File(file), append)
    writer
      .append(s)
      .append("\n")
      .flush()

    writer.close()
  }


}
