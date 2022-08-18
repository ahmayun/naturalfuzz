package examples.clustermonitor

import fuzzer.ProvInfo
import org.apache.spark.{SparkConf, SparkContext}
import provenance.rdd.ProvenanceRDD.toPairRDD
import sparkwrapper.SparkContextWithDP
import taintedprimitives.SymImplicits._
import taintedprimitives.{TaintedInt, TaintedString}

import scala.collection.mutable.ListBuffer

object WordCount extends Serializable {
  def main(args: Array[String]): ListBuffer[ListBuffer[(Int,Int,Int)]] = {
    println(s"WordCount args ${args.mkString(",")}")
    val sparkConf = new SparkConf()
    if (args.length < 2) throw new IllegalArgumentException("Program was called with too few args")
    sparkConf.setMaster(args(1))
    sparkConf.setAppName("Prov WordCount")
    val ctx = new SparkContextWithDP(new SparkContext(sparkConf))
    ctx.setLogLevel("ERROR")
    val (rdd1, depsInfo1) = _root_.monitoring.Monitors.monitorReduceByKey(ctx.textFileProv(args(0), _.split("\\s"))
      .flatMap(s => s)
      .map { s => (s, 1) }, sumFunc, 1)

    rdd1
      .collect()
      .foreach(e => println(s"final: $e"))

    println(depsInfoToString(depsInfo1))
    println(depsInfo1.length)
    _root_.monitoring.Monitors.finalizeProvenance(depsInfo1)
  }

  def depsInfoToString(depsInfo: ListBuffer[ListBuffer[(Int,Int,Int)]]): String = {
    depsInfo
      .map{
        deps =>
          val row = deps.map{case (ds, row, col) => s"($ds,$row,$col)"}.mkString("<=>")
          s"$row"
      }.mkString("\n----------------------------\n")
  }

  def sumFunc(a: Int, b: Int): Int = {
    a + b
  }
}