package refactor.testout
import org.apache.spark.{SparkConf, SparkContext}
import provenance.data.Provenance
import sparkwrapper.SparkContextWithDP


object predicateTest2 {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Test")
    val ctx = new SparkContextWithDP(new SparkContext(sparkConf))
    Provenance.setProvenanceType("dual")
    val data1 = ctx.textFileProv("dummydata/predicateTest2", _.split(","))
    val data2 = ctx.textFileProv("dummydata/predicateTest2", _.split(","))
    _root_.monitoring.Monitors.monitorFilter(_root_.monitoring.Monitors.monitorJoin(data1, data2, 194), {
      case (k, (_, _)) =>
        k.toInt > 3 && k.toInt < 6
    }, 186).foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }
}