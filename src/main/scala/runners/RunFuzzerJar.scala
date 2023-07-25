package runners

import fuzzer.Fuzzer.writeToFile
import fuzzer._
import guidance.ProvFuzzGuidance
import org.apache.spark.{SparkConf, SparkContext}
import refactor.SparkProgramTransformer
import runners.RunRIGFuzzJar.{checkMembership, createSavedJoins, generateList, printIntermediateRDDs}
import symbolicexecution.{SymExResult, SymbolicExecutor, SymbolicExpression}
import utils.MiscUtils.toBinaryStringWithLeadingZeros
import utils.{Pickle, QueryResult, RIGUtils}

import scala.collection.mutable.ListBuffer


object RunFuzzerJar {

  def main(args: Array[String]): Unit = {

    val (benchmarkName, sparkMaster, duration, outDir, inputFiles) = if (!args.isEmpty) {
      (args(0), args(1), args(2), args(3), args.takeRight(args.length-4))
    } else {
//      val name = "WebpageSegmentation"
//      val Some(files) = Config.mapInputFilesReduced.get(name)
//      (name, "local[*]", "20", s"target/depfuzz-output/$name", files)
      val name = "Q1"
      val _mutantName = "Q1"
      (name,
        "local[*]",
        "20",
        "<not used, placeholder>",
        Array("store_returns", "date_dim", "store", "customer").map(s => s"/home/ahmad/Documents/VT/project2/tpcds-datagen/data_csv_no_header/$s"))
    }
//    val Some(funFuzzable) = Config.mapFunFuzzables.get(benchmarkName)
//    val Some(codepInfo) = Config.provInfos.get(benchmarkName)
    val outPathInstrumented = "src/main/scala/examples/instrumented"
    val outPathFWA = "src/main/scala/examples/fwa"

    val instPackage = "examples.instrumented"
    val instProgramClass = s"$instPackage.$benchmarkName"
    val instProgramPath = s"$outPathInstrumented/$benchmarkName.scala"

    val sc = new SparkContext(
      new SparkConf()
        .setAppName("NaturalFuzz")
        .setMaster(sparkMaster)
    )

    val expressionAccumulator = sc.collectionAccumulator[SymbolicExpression]("ExpressionAccumulator")

    val symProgram = new DynLoadedProgram[SymExResult](
      benchmarkName,
      instProgramClass,
      instProgramPath,
      inputFiles,
      expressionAccumulator,
      {
        case Some(expressions) => expressions.asInstanceOf[SymExResult]
        case _ => null
      }
    )


    // Preprocessing and Fuzzing
    println("Running monitored program")
    val pathExpressions = symProgram.invokeMain(symProgram.args)
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

    val rawDS = inputFiles
      .map(sc.textFile(_))

    val preJoinFill = branchConditions.createSatVectors(rawDS)

    printIntermediateRDDs("Pre Join Path Vectors:", preJoinFill, branchConditions)

    val savedJoins = createSavedJoins(preJoinFill, branchConditions)
    println("Saved Joins")
    if (savedJoins.nonEmpty) {
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
                .takeSample(withReplacement = false, 10).toSeq
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

    def createSafeFileName(pname: String, pargs: Array[String]): String = {
      s"$pname"
      //s"${pname}_${pargs.map(_.split("/").last).mkString("-")}"
    }

    val foldername = createSafeFileName(benchmarkName, inputFiles)
    Pickle.dump(qrs, s"./pickled/qrs/$foldername.pkl")
    finalReduced.zipWithIndex.map { case (e, i) => writeToFile(s"./pickled/reduced_data/$foldername", e, i) }

  }

  def reportStats(program: ExecutableProgram, stats: FuzzStats, timeStartFuzz: Long, timeEndFuzz: Long): Unit = {
    val durationProbe = 0.1f // (timeEndProbe - timeStartProbe) / 1000.0
    val durationFuzz = (timeEndFuzz - timeStartFuzz) / 1000.0
    val durationTotal = durationProbe + durationFuzz

    // Printing results
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(program.name, msg.mkString(","))} $c x $msg") }
    stats.failureMap.foreach { case (msg, (_, c, i)) => println(s"i=$i:line=${getLineNo(program.name, msg.mkString(","))} x $c") }
    stats.failureMap.map { case (msg, (_, c, i)) => (getLineNo(program.name, msg.mkString("\n")), c, i) }
      .groupBy(_._1)
      .map { case (line, list) => (line, list.size) }
      .toList.sortBy(_._1)
      .foreach(println)

    println(s"=== RESULTS: ProvFuzz ${program.name} ===")
    println(s"failures: ${stats.failureMap.map { case (_, (_, _, i)) => i + 1 }.toSeq.sortBy(i => i).mkString(",")}")
    println(s"# of Failures: ${stats.failures} (${stats.failureMap.keySet.size} unique)")
    println(s"coverage progress: ${stats.plotData._2.map(limitDP(_, 2)).mkString(",")}")
    println(s"iterations: ${Global.iteration}")
    println(s"Total Time (s): ${limitDP(durationTotal, 2)} (P: $durationProbe | F: $durationFuzz)")
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
}
