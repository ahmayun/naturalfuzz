package examples.monitored

import fuzzer.ProvInfo
import org.apache.spark.{SparkConf, SparkContext}
import provenance.data.Provenance
import sparkwrapper.SparkContextWithDP
import taintedprimitives.{TaintedFloat, TaintedInt, TaintedString}
import taintedprimitives.SymImplicits._
object DeliveryFaults {
  def main(args: Array[String]): ProvInfo = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("Delivery Faults")
    val sc = new SparkContextWithDP(new SparkContext(conf))
    Provenance.setProvenanceType("dual")
    val deliveries = sc.textFileProv(args(0),_.split(',')).map(r => (r(0), (r(1), r(2), r(3).toFloat)))
    val same_deliveries = _root_.monitoring.Monitors.monitorGroupByKey(deliveries, 0)
    val triplets = same_deliveries.filter(_._2.size > 2)
    val bad_triplets = triplets.filter(tup => tripletRating(tup) < 2.0f)
    bad_triplets.map(processTriplets).collect().foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }
  def tripletRating(tup: (TaintedString, Iterable[(TaintedString, TaintedString, TaintedFloat)])): TaintedFloat = {
    val (_, iter) = tup
    iter.foldLeft(0.0f)({
      case (acc, (_, _, rating)) =>
        rating + acc
    }) / iter.size
  }
  def processTriplets(tup: (TaintedString, Iterable[(TaintedString, TaintedString, TaintedFloat)])): String = {
    val (_, iter) = tup
    iter.foldLeft("")({
      case (acc, (_, vendor, _)) =>
        s"$acc,$vendor"
    })
  }
}