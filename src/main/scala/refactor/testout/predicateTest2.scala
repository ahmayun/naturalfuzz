package refactor.testout
import org.apache.spark.{SparkConf, SparkContext}
import sparkwrapper.SparkContextWithDP
object predicateTest2 {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Test")
    val ctx = new SparkContextWithDP(new SparkContext(sparkConf))
    val data1 = ctx.textFileProv("dummydata/predicateTest2", _.split(","))
    val data2 = ctx.textFileProv("dummydata/predicateTest2", _.split(","))
    val d1 = data1.map(arr => (arr.head, arr.tail))
    val d2 = data2.map(arr => (arr.head, arr.tail))
    _root_.monitoring.Monitors.monitorJoin(d1, d2, 1).filter({
      case (k, (_, _)) =>
        k.toInt > 3 && k.toInt < 6
    }).foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }
}