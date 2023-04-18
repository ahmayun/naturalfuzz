package examples.mutants.Q6
import abstraction.{ SparkConf, SparkContext }
import capture.IOStreams._println
object Q6_M6 extends Serializable {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 6")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
    val MONTH = 1
    val YEAR = 2001
    val customer_address = sc.textFile(args(0)).map(_.split(","))
    val customer = sc.textFile(args(1)).map(_.split(","))
    val store_sales = sc.textFile(args(2)).map(_.split(","))
    val date_dim = sc.textFile(args(3)).map(_.split(","))
    val item = sc.textFile(args(4)).map(_.split(","))
    val filter1 = date_dim.filter {
      row => row(6).toInt == YEAR && row(8).toInt == MONTH
    }
    filter1.take(10).foreach(_println)
    val map1 = filter1.map(row => row(3))
    val distinct = map1.distinct
    val take1 = distinct.take(1).head
    val map2 = customer_address.map(row => (row.head, row))
    val map3 = customer.map(row => (row(4), row))
    val join1 = map2.join(map3)
    join1.take(10).foreach(_println)
    val map4 = join1.map({
      case (addr_sk, (ca_row, c_row)) =>
        (c_row.head, (ca_row, c_row))
    })
    val map5 = store_sales.map(row => (row(2), row))
    val join2 = map4.join(map5)
    join2.take(10).foreach(_println)
    val map6 = join2.map({
      case (customer_sk, ((ca_row, c_row), ss_row)) =>
        (ss_row.last, (ca_row, c_row, ss_row))
    })
    val map7 = date_dim.map(row => (row.head, row))
    val join3 = map6.join(map7)
    join3.take(10).foreach(_println)
    val map8 = join3.map({
      case (date_sk, ((ca_row, c_row, ss_row), dd_row)) =>
        (ss_row(1), (ca_row, c_row, ss_row, dd_row))
    })
    val map9 = item.map(row => (row.head, row))
    val join4 = map8.join(map9)
    join4.take(10).foreach(_println)
    val map10 = join4.map({
      case (item_sk, ((ca_row, c_row, ss_row, dd_row), i_row)) =>
        (ca_row, c_row, ss_row, dd_row, i_row)
    })
    val map11 = map10.map({
      case (_, _, _, _, i_row) =>
        (convertColToFloat(i_row, 5), 1)
    })
    val reduce1 = map11.reduce({
      case ((v1, c1), (v2, c2)) =>
        (v1 + v2, c1 + c2)
    })
    _println(s"reduce1 = $reduce1")
    val subquery2_result = reduce1._1 / reduce1._2
    _println(s"subquery2 result = $subquery2_result")
    val filter2 = map10.filter(tup => tup._4(3) == take1)
    filter2.take(10).foreach(_println)
    val filter3 = filter2.filter({
      case (_, _, _, _, i_row) =>
        val i_current_price = convertColToFloat(i_row, 5)
        i_current_price != 1.2d * subquery2_result
    })
    filter3.take(10).foreach(_println)
    val map12 = filter3.map({
      case (ca_row, c_row, ss_row, dd_row, i_row) =>
        (try {
          ca_row(8)
        } catch {
          case _ => "NULL"
        }, 1)
    })
    val rbk1 = map12.reduceByKey(_ + _)
    rbk1.take(10).foreach(_println)
    val filter4 = rbk1.filter({
      case (state, count) =>
        count > 10
    })
    filter4.take(10).foreach(_println)
    val sortBy1 = filter4.sortBy(_._2)
    val take2 = sortBy1.take(10)
    val sortWith1 = take2.sortWith({
      case (a, b) =>
        a._2 < b._2 || a._2 == b._2 && a._1 < b._1
    })
    sortWith1.foreach({
      case (state, count) =>
        _println(state, count)
    })
  }
  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
    }
  }
}