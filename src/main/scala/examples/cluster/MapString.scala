package examples.cluster

import abstraction.{SparkConf, SparkContext}

object MapString {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    if (args.length < 2) throw new IllegalArgumentException("Program was called with too few args")
    conf.setMaster(args(1))
    conf.setAppName("WordCount")

    val sc = new SparkContext(conf)
    sc.textFile(args(0)).map { s =>
      s
    }
      .take(100)
      .foreach(println)
  }
}