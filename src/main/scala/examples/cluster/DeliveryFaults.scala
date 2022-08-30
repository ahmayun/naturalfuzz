package examples.cluster

import abstraction.{SparkConf, SparkContext}

object DeliveryFaults {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    if (args.length < 2) throw new IllegalArgumentException("Program was called with too few args")
    conf.setMaster(args(1))
    conf.setAppName("Delivery Faults")
    val sc = new SparkContext(conf)

    //<delivery_id>,<customer_id>,<vendor>,<rating>
    val deliveries = sc.textFile(args(0))
      .map(_.split(','))
      .map(r => (r(0), (r(1), r(2), r(3))))

    val same_deliveries = deliveries.groupByKey()
    val triplets = same_deliveries.filter {
      group =>
        val ret = group._2.size > 2
        ret
    }

    val bad_triplets = triplets.filter{
      tup =>
        tripletRating(tup) < 2.0f
    }

    bad_triplets
      .map(processTriplets)
      .collect()
      .foreach(println)
  }

  def tripletRating(tup: (String, Iterable[(String, String, String)])): Float = {
    val (_, iter) = tup
    iter.foldLeft(0.0f){case (acc, (_, _, rating)) =>
      rating.toInt + acc}/iter.size
  }

  def processTriplets(tup: (String, Iterable[(String, String, String)])): String = {
    val (_, iter) = tup
    iter.foldLeft(""){case (acc,(_, vendor,_)) =>
      s"$acc,$vendor"}
  }
}