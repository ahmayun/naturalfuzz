package generators

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

object GenFlightData extends Serializable {

  def generateString(len: Int): String = {
    Random.alphanumeric.take(len).mkString
  }

  def main(args: Array[String]): Unit = {
    val partitions = args(0).toInt
    val dataper = args(1).toInt
    val name = s"${args(2)}_${partitions*dataper}"
    val seed = Random.nextLong()
    Random.setSeed(seed)

    val sparkConf = new SparkConf()
    val datasets = Array(
      ("ds1", s"hdfs://zion-headnode:9000/ahmad/$name/airports"),
      ("ds2", s"hdfs://zion-headnode:9000/ahmad/$name/flights")
    )
    sparkConf.setMaster("spark://zion-headnode:7077")
    sparkConf.setAppName("DataGen: Customers")

    println(
      s"""
         |partitions: $partitions
         |records: $dataper
         |seed: $seed
         |""".stripMargin
    )
    //    val fault_rate = 0.0001
    //    def faultInjector()  = if(Random.nextInt(dataper*partitions) < dataper*partitions* fault_rate) true else false

    datasets.foreach { case (ds, f) =>
      ds match {
        case "ds1" =>
          SparkContext.getOrCreate(sparkConf).parallelize(Seq[Int](), partitions).mapPartitions { _ =>
            (1 to dataper).map { _ =>
              // KGD,"Khrabrovo Airport",Kaliningrad,20.5925998687744,54.8899993896484,Europe/Kaliningrad
              val airportCode = ""
              val airportName = ""
              val long = ""
              val lat = ""
              val continent = ""
              s"""$airportCode,$airportName,$long,$lat,$continent"""
            }.iterator
          }.saveAsTextFile(f)

        case "ds2" =>
          SparkContext.getOrCreate(sparkConf).parallelize(Seq[Int](), partitions).mapPartitions { _ =>
            (1 to dataper).map { _ =>
              // 32658,PG0674,"2017-08-19 02:35:00.000","2017-08-19 05:00:00.000",KRO,KJA,Scheduled,CR2,,
              val fid = ""
              val pgid = ""
              val arrivalTime = ""
              val depTime = ""
              val arrivalAirport = ""
              val depAirport = ""
              val status = ""
              val idk = ""
              s"""$fid,$pgid,$arrivalTime,$depTime,$arrivalAirport,$depAirport,$status,$idk"""
            }.iterator
          }.saveAsTextFile(f)
      }
    }
  }

}