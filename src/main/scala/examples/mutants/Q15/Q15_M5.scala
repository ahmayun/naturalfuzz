package examples.mutants.Q15
import abstraction.{ SparkConf, SparkContext }
import capture.IOStreams._println
object Q15_M5 extends Serializable {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://zion-headnode:7077")
    sparkConf.setAppName("TPC-DS Query 15")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
    val YEAR = 1999
    val QOY = 1
    val ZIPS = List("85669", "86197", "88274", "83405", "86475", "85392", "85460", "80348", "81792")
    val STATES = List("CA", "WA", "GA")
    val p = "/TPCDS_1G_NOHEADER_NOCOMMAS"
    args(0) = s"$p/catalog_sales"
    val catalog_sales = sc.textFile(args(0)).map(_.split(","))
    args(1) = s"$p/customer"
    val customer = sc.textFile(args(1)).map(_.split(","))
    args(2) = s"$p/customer_address"
    val customer_address = sc.textFile(args(2)).map(_.split(","))
    args(3) = s"$p/date_dim"
    val date_dim = sc.textFile(args(3)).map(_.split(","))
    val filtered_dd = date_dim.filter { row => 
      val d_qoy = row(10)
      val d_year = row(6)
      d_qoy == QOY.toString && d_year == YEAR.toString
    }
    filtered_dd.take(10).foreach(_println)
    val map1 = catalog_sales.map(row => (row(2), row))
    val map2 = customer.map(row => (row.head, row))
    val join1 = map1.join(map2)
    join1.take(10).foreach(_println)
    val map3 = join1.map({
      case (_, (cs_row, c_row)) =>
        (c_row(4), (cs_row, c_row))
    })
    val map4 = customer_address.map(row => (row.head, row))
    val join2 = map3.join(map4)
    join2.take(10).foreach(_println)
    val map5 = join2.map({
      case (_, ((cs_row, c_row), ca_row)) =>
        (cs_row.last, (cs_row, c_row, ca_row))
    })
    val filter1 = map5.filter({
      case (_, (cs_row, c_row, ca_row)) =>
        val ca_zip = getColOrEmpty(ca_row, 9)
        val ca_state = getColOrEmpty(ca_row, 8)
        val cs_sales_price = convertColToFloat(cs_row, 20)
        ca_zip != "error" && ca_state != "error" && (ZIPS.contains(ca_zip.take(5)) || cs_sales_price > 500 || STATES.contains(ca_state))
    })
    filter1.take(10).foreach(_println)
    val map6 = filtered_dd.map(row => (row.head, row))
    val join3 = filter1.join(map6)
    join3.take(10).foreach(_println)
    val map7 = join3.map({
      case (_, ((cs_row, c_row, ca_row), dd_row)) =>
        val cs_sales_price = convertColToFloat(cs_row, 20)
        (ca_row(9), cs_sales_price)
    })
    val rbk1 = map7.reduceByKey(_ / _)
    rbk1.take(10).foreach(_println)
    val sortBy1 = rbk1.sortBy(_._1)
    sortBy1.take(10).foreach(_println)
  }
  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
    }
  }
  def getColOrEmpty(row: Array[String], col: Int): String = {
    try {
      row(col)
    } catch {
      case _: Throwable => "error"
    }
  }
}