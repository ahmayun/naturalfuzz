package generators

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

object GenWordCountData extends Serializable {

  val partitions = 2
  val dataper  = 100000
  val seed = Random.nextLong()

  def generateURL(len: Int): String = {
    s"www.${Random.alphanumeric.take(len).mkString}.com"
  }

  def getRandomComponentType: String = {
    val types = Array(
      "advertisement",
      "header",
      "footer",
      "heading",
      "logo"
    )
    types(Random.nextInt(types.length))
  }

  def main(args:Array[String]): Unit = {
      Random.setSeed(seed)

      val sparkConf = new SparkConf()
      val datasets = Array(
        ("ds1", "hdfs://zion-headnode:9000/ahmad/WordCount")
      )
      sparkConf.setMaster("spark://zion-headnode:7077")
      sparkConf.setAppName("DataGen: WordCounts")

      println(
      s"""
         |partitions: $partitions
         |records: $dataper
         |seed: $seed
         |""".stripMargin
      )

    datasets.foreach { case (_, f) =>
      SparkContext.getOrCreate(sparkConf).parallelize(Seq[Int]() , partitions).mapPartitions { _ =>
        (1 to dataper).map{_ =>
          s"This is a sentence"
        }.iterator}.saveAsTextFile(f)
    }
  }

}