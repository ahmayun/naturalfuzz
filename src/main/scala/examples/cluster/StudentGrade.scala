package examples.cluster

import abstraction.{SparkConf, SparkContext}

object StudentGrade {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    if (args.length < 2) throw new IllegalArgumentException("Program was called with too few args")
    conf.setMaster(args(1))
    conf.setAppName("StudentGrade")

    val sc = new SparkContext(conf)

    val data = sc.textFile(args(0)).map(_.split(",")) // "datasets/fuzzing_seeds/commute/trips"
      .map { a =>
        val ret = (a(0), a(1).toInt)
        ret
      }
      .map { a =>
        if (a._2 > 40)
          (a._1 + " Pass", 1)
        else
          (a._1 + " Fail", 1)
      }
      .reduceByKey{
        (a, b) =>
          val ret = a+b
          ret
      }
      .filter { v =>
        v._2 > 5
      }.collect()
      .foreach{case (a, b) => println(s"$a, $b")}
  }
}