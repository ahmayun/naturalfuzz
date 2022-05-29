package examples.monitored

import fuzzer.ProvInfo

import scala.reflect.runtime.universe._
import org.apache.spark.{SparkConf, SparkContext}
import provenance.data.Provenance
import sparkwrapper.SparkContextWithDP
import symbolicprimitives.SymInt
import symbolicprimitives.SymImplicits._
object Delays {
  def main(args: Array[String]): ProvInfo = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("Bus Delays")
    val sc = new SparkContextWithDP(new SparkContext(conf))
    Provenance.setProvenanceType("dual")
    val station1 = sc.textFileProv(args(0),_.split(',')).map(r => (r(0), (r(1).toInt, r(2).toInt, r(3))))
    val station2 = sc.textFileProv(args(1),_.split(',')).map(r => (r(0), (r(1).toInt, r(2).toInt, r(3))))
    val joined = _root_.monitoring.Monitors.monitorJoin(0, station1, station2)
    val mapped = joined.map({
      case (_, ((dep, adep, rid), (arr, aarr, _))) =>
        (buckets((arr-aarr) - (dep-adep)), rid)
    })
    val grouped = _root_.monitoring.Monitors.monitorGroupByKey(0, mapped)
    val filtered = grouped.filter(_._1 > 2).flatMap(_._2).map((_, 1))
    val reduced = filtered.reduceByKey(_+_)
    reduced.collect().foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }
  def buckets(v: SymInt): SymInt = {
    v / 1800
  }
}