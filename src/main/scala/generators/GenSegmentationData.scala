package generators

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

object GenSegmentationData {

  val partitions = 2
  val dataper  = 10000
  val url_len = 2
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
        ("before", "datasets/bigdata/webpage_segmentation/before"),
        ("after", "datasets/bigdata/webpage_segmentation/after")
      )
      sparkConf.setMaster("local[*]")
      sparkConf.setAppName("Webpage Segmentation Data Generator").set("spark.executor.memory", "2g")

      println(
      s"""
         |partitions: $partitions
         |records: $dataper
         |url len: $url_len
         |seed: $seed
         |""".stripMargin
      )
//    val fault_rate = 0.0001
//    def faultInjector()  = if(Random.nextInt(dataper*partitions) < dataper*partitions* fault_rate) true else false

    datasets.foreach{ case (_, f) =>
      if(new File(f).exists()){
        deleteDir(new File(f))
    }}

    val sc = new SparkContext(sparkConf)
    datasets.foreach { case (_, f) =>
      sc.parallelize(Seq[Int]() , partitions).mapPartitions { _ =>
        (1 to dataper).map{_ =>
          val url = generateURL(url_len)
          val swx = Random.nextInt(1920)
          val swy = Random.nextInt(1080)
          val w = Random.nextInt(1920)
          val h = Random.nextInt(1080)
          val cid = Random.nextInt(10)
          val ctype = getRandomComponentType
          s"""$url,$swx,$swy,$h,$w,$cid,$ctype"""
        }.iterator}.saveAsTextFile(f)
    }
  }

  def deleteDir(file: File): Unit = {
    val contents = file.listFiles
    if (contents != null) for (f <- contents) {
      deleteDir(f)
    }
    file.delete
  }

}