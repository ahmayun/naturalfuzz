package refactor.testout
import org.apache.spark.{ SparkConf, SparkContext }
object predicateTest2 {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("Test")
    val ctx = new SparkContext(sparkConf)
    val data1 = ctx.parallelize(Seq[Int]()).mapPartitions {
      _ => (1 to 10).map {
        k => (s"$k", "value")
      }.iterator
    }
    val data2 = ctx.parallelize(Seq[Int]()).mapPartitions {
      _ => (1 to 10).map {
        k => (s"$k", "value")
      }.iterator
    }
    data1.join(data2).filter({
      case (k, (_, _)) =>
        k.toInt > 3 && k.toInt < 6
    }).foreach(println)
    _root_.monitoring.Monitors.finalizeProvenance()
  }
}