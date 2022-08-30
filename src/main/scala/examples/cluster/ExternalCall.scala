package examples.cluster

import abstraction.{SparkConf, SparkContext}

import scala.math.log10

object ExternalCall {

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
      .filter{ v =>
        val v1 = log10(v._2)
        v1 > 1
      }
      .take(5)
      .foreach(println)
  }
}