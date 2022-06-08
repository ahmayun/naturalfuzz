package examples.monitored

import fuzzer.ProvInfo

import scala.reflect.runtime.universe._
import org.apache.spark.{SparkConf, SparkContext}
import provenance.data.Provenance.setProvenanceType
import sparkwrapper.SparkContextWithDP
import taintedprimitives.Utils
object CommuteType {
  def main(args: Array[String]): ProvInfo = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("CommuteTime")
    val data1 = Array(",, ,0,1", ",, ,16,1", ",, ,41,1", " , , ,", " , , , ,0", " , , , ,", "", "", "", ",A, ,-0,1", ",A, ,-0,1")
    val data2 = Array(",Palms", ",Palms", ",Palms", "", "", "", "", ",", ",", "", "")
    val sco = new SparkContext(conf)
    val sc = new SparkContextWithDP(sco)
    setProvenanceType("dual")
    val tripLines = sc.textFileProv(args(0)) //"datasets/commute/trips/part-000[0-4]*"
    try {
      val trips = tripLines.map { s => 
        val cols = s.split(",")
        (cols(1), Integer.parseInt(cols(3)) / Integer.parseInt(cols(4)))
      }
      val types = trips.map { s => 
        val speed = s._2
        if (_root_.monitoring.Monitors.monitorPredicate(speed > 40, (List[Any](speed), List[Any]()), 0)) {
          ("car", speed)
        } else if (_root_.monitoring.Monitors.monitorPredicate(speed > 15, (List[Any](speed), List[Any](speed)), 1)) {
          ("public", speed)
        } else {
          ("onfoot", speed)
        }
      }
      val out = types.aggregateByKey((0.0d, 0))({
        case ((sum, count), next) =>
          (sum + next, count + 1)
      }, {
        case ((sum1, count1), (sum2, count2)) =>
          (sum1 + sum2, count1 + count2)
      }).mapValues({
        case (sum, count) =>
          sum.toDouble / count
      }).collect()
    } catch {
      case e: Exception =>
        e.printStackTrace()
    }
    sco.stop()
    _root_.monitoring.Monitors.finalizeProvenance()
  }
}