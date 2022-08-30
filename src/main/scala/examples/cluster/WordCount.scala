package examples.cluster

import abstraction.{SparkConf, SparkContext}

object WordCount {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    if (args.length < 2) throw new IllegalArgumentException("Program was called with too few args")
    conf.setMaster(args(1))
    conf.setAppName("WordCount")

    val sc = new SparkContext(conf)
    sc.textFile(args(0)).flatMap(_.split("\\s")) // "datasets/fuzzing_seeds/commute/trips"
      .map { s =>
        (s,1)
      }
      .reduceByKey { (a, b) =>
        val sum = a+b
        sum
      }// Numerical overflow
      .collect()
      .foreach(println)
  }
}