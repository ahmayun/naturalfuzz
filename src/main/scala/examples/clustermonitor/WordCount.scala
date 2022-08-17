package examples.clustermonitor

import fuzzer.ProvInfo
import org.apache.spark.{SparkConf, SparkContext}
import provenance.rdd.ProvenanceRDD.toPairRDD
import sparkwrapper.SparkContextWithDP
import taintedprimitives.SymImplicits._
import taintedprimitives.{TaintedInt, TaintedString}

object WordCount extends Serializable {
  def main(args: Array[String]): ProvInfo = {
    println(s"WordCount args ${args.mkString(",")}")
    val sparkConf = new SparkConf()
    if (args.length < 3) throw new IllegalArgumentException("Program was called with too few args")
    sparkConf.setMaster(args(2))
    sparkConf.setAppName("Prov WordCount")
    val ctx = new SparkContextWithDP(new SparkContext(sparkConf))
    ctx.setLogLevel("ERROR")
    _root_.monitoring.Monitors.monitorReduceByKey(ctx.textFileProv(args(0), _.split("\\s")).flatMap(s => s).map {
      s => (s, 1)
    }, sumFunc, 1).collect().foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }

  def sumFunc(a: Int, b: Int): Int = {
    a + b
  }
}