package runners

import fuzzer.{NewFuzzer, Global, Program, SymbolicProgram}
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
import java.net.InetAddress

object RunRIGFuzzJarFuzzing extends Serializable {

  def main(args: Array[String]): Unit = {

    println("RunRIGFuzzJar called with following args:")
    println(args.mkString("\n"))
    var onCluster = false

    // ==P.U.T. dependent configurations=======================
    val (benchmarkName, mutantName, sparkMaster, pargs, duration, outDir, pickleDir) =
      if (!args.isEmpty) {
        (args(0),
          args(1),
          args(2),
          args.takeRight(args.length - 6),
          args(3),
          args(4),
          args(5)
        )
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
//          s"target/rig-output-local/${_mutantName}",
//          if(System.getProperty("user.name") == "ahmad") "./pickled" else { onCluster = true; "/home/student/pickled/qrs" })
//        val name = "Q1"
//        val _mutantName = "Q1"
//        (name,
//          _mutantName,
//          "local[1]",
//          Array("0", "1", "2", "3").map(s => s"./seeds/rig_reduced_data/$name/dataset_$s"),
//          "20",
//          s"target/rig-output-local/${_mutantName}",
//          if(System.getProperty("user.name") == "ahmad") "./pickled" else { onCluster = true; "/home/student/pickled/qrs" })
        //        val name = "Delays"
        //        (name, "local[*]",
        //          Array("station1", "station2").map { s => s"seeds/reduced_data/delays/$s" },
        //          "30",
        //          s"target/rig-output-local/$name")

        val name = "Q3"
        val _mutantName = "Q3_M0"
        (name,
          _mutantName,
          "local[1]",
          Array("0", "1", "2").map(s => s"./seeds/rig_reduced_data/$name/dataset_$s"),
          "20",
          s"target/rig-output-local/${_mutantName}",
          if (System.getProperty("user.name") == "ahmad") "./pickled" else {
            onCluster = true; "/home/student/pickled/qrs"
          })
      }
    if(System.getProperty("user.name") != "ahmad") {
      onCluster = true
    }

    println("onCluster=",onCluster)
    Config.benchmarkName = benchmarkName
    Config.sparkMaster = sparkMaster
    val Some(funFaulty) = Config.mapFunFuzzables.get(benchmarkName)
    val Some(schema) = Config.mapSchemas.get(benchmarkName)
    val benchmarkClass = s"examples.faulty.$benchmarkName"
    val mutantClass = s"examples.mutants.$benchmarkName.$mutantName"
    // ========================================================

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

    def createSafeFileName(pname: String, pargs: Array[String]): String = {
      s"$pname"
      //s"${pname}_${pargs.map(_.split("/").last).mkString("-")}"
    }


    val qrs = Pickle.load[List[QueryResult]](s"${pickleDir}/${createSafeFileName(benchmarkName, pargs)}.pkl")
    qrs.foreach {
      qr =>
        println(s"====QR: ${qr.query.map(_.tree).mkString(" <=>")} ===== ")
        qr.filterQueryRDDs
          .zipWithIndex
          .foreach {
            case (rdd, i) =>
              println(s"=> RDD ${i}")
              println(rdd.mkString("\n"))
        }
    }
    val guidance = new RIGGuidance(pargs, schema, duration.toInt, new QueriedRDDs(qrs))

    val (stats, timeStartFuzz, timeEndFuzz) = NewFuzzer.FuzzMutants(program, mutantProgram, guidance, outDir, !onCluster)

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
    println(
      s"Config:\n" +
        s"\tProgram: ${program.name}\n" +
        s"\tIterations: ${Global.iteration}"
    )

  }

}
