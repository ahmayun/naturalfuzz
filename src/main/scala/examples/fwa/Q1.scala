package examples.fwa
import abstraction.{ SparkConf, SparkContext }
object Q1 extends Serializable {
  def main(args: Array[String]): Unit = {
    println(s"Q1 called with args:\n${args.mkString("""
""")}")
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("TPC-DS Query 1")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
    val YEAR = 1999
    val STATE = "TN"
    val Array(store_returns, date_dim, store, customer) = if (!args.isEmpty) {
      println("loading ds1")
      val store_returns = try {
        sc.textFile(args(0)).map(_.split(","))
      } catch {
        case e: Throwable =>
          println(s"loading error: $e")
          null
      }
      println("loading ds2")
      val date_dim = sc.textFile(args(1)).map(_.split(","))
      println("loading ds3")
      val store = sc.textFile(args(2)).map(_.split(","))
      println("loading ds4")
      val customer = sc.textFile(args(3)).map(_.split(","))
      Array(store_returns, date_dim, store, customer)
    } else {
      Array("store_returns", "date_dim", "store", "customer").map(s => sc.textFile(s"/home/ahmad/Documents/VT/project2/tpcds-datagen/data_csv_no_header/$s").map(_.split(",")))
    }
    val filter1 = date_dim.filter(row => row(6).toInt == YEAR)
    println("filter1")
    filter1.take(10).foreach(println)
    val map1 = filter1.map(row => (row.head, row))
    println("map1")
    map1.take(10).foreach(println)
    val map2 = store_returns.map(row => (row.last, row))
    println("map2")
    map2.take(10).foreach(println)
    val join1 = map2.join(map1)
    println("join1")
    join1.take(10).foreach(println)
    val map3 = join1.map { row => 
      val store_returns_row = row._2._1
      val sr_customer_sk = store_returns_row(2)
      val sr_store_sk = store_returns_row(6)
      val sum_agg_field = List(10, 11, 12, 13, 15, 16, 17).map(n => convertColToFloat(store_returns_row, n)).reduce(_ + _)
      ((sr_customer_sk, sr_store_sk), (sr_customer_sk, sr_store_sk, sum_agg_field))
    }
    println("map3")
    map3.take(10).foreach(println)
    val rbk1 = map3.reduceByKey({
      case ((r1c1, r1c2, sum1), (r2c1, r2c2, sum2)) =>
        (r1c1, r2c2, sum1 + sum2)
    })
    println("rbk1")
    rbk1.take(10).foreach(println)
    val map4 = rbk1.map({
      case ((sr_customer_sk, sr_store_sk), rest) =>
        (sr_store_sk, rest)
    })
    println("map4")
    map4.take(10).foreach(println)
    val map10 = store.map(row => (row.head, row))
    val join2 = map4.join(map10)
    println("join2")
    join2.take(10).foreach(println)
    val map5 = join2.map({
      case (store_sk, (ctr_row @ (sr_customer_sk, st_store_sk, sum_agg_field), store_row)) =>
        (sr_customer_sk, (ctr_row, store_row))
    })
    println("map5")
    map5.take(10).foreach(println)
    val map9 = customer.map(row => (row.head, row))
    val join3 = map5.join(map9)
    println("join3")
    join3.take(10).foreach(println)
    val map6 = join3.map({
      case (customer_sk, ((ctr_row, store_row), customer_row)) =>
        (ctr_row, store_row, customer_row)
    })
    println("map6")
    map6.take(10).foreach(println)
    val map7 = map6.map({
      case ((_, _, total_return), _, _) =>
        (total_return, 1)
    })
    println("map7")
    map7.take(10).foreach(println)
    val reduce1 = map7.reduce({
      case ((v1, c1), (v2, c2)) =>
        (v1 + v2, c1 + c2)
    })
    println("reduce1")
    println(reduce1)
    val avg = reduce1._1 / reduce1._2.toFloat * 1.2f
    val filter2 = map6.filter({
      case ((_, _, return_total), store_row, _) =>
        return_total > avg && store_row(24) == STATE
    })
    println("filter2")
    filter2.take(10).foreach(println)
    val map8 = filter2.map({
      case (_, _, customer_row) =>
        customer_row(1)
    })
    println("map8")
    map8.take(10).foreach(println)
  }
  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
    }
  }
}