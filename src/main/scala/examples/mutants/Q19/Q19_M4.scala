package examples.mutants.Q19
import abstraction.{ SparkConf, SparkContext }
import capture.IOStreams._println
object Q19_M4 extends Serializable {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 19").setMaster("spark://zion-headnode:7077")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
    val YEAR = 1999
    val MONTH = 11
    val MANAGER = "50"
    val p = "/TPCDS_1G_NOHEADER_NOCOMMAS"
    args(0) = s"$p/date_dim"
    val date_dim = sc.textFile(args(0)).map(_.split(","))
    args(1) = s"$p/store_sales"
    val store_sales = sc.textFile(args(1)).map(_.split(","))
    args(2) = s"$p/item"
    val item = sc.textFile(args(2)).map(_.split(","))
    args(3) = s"$p/customer"
    val customer = sc.textFile(args(3)).map(_.split(","))
    args(4) = s"$p/customer_address"
    val customer_address = sc.textFile(args(4)).map(_.split(","))
    args(5) = s"$p/store"
    val store = sc.textFile(args(5)).map(_.split(","))
    val filtered_i = item.filter { row => 
      val i_manager_id = row(row.length - 2)
      i_manager_id == MANAGER
    }
    filtered_i.take(10).foreach(_println)
    val filtered_dd = date_dim.filter { row => 
      val d_moy = row(8)
      val d_year = row(6)
      d_moy == MONTH.toString && d_year == YEAR.toString
    }
    filtered_dd.take(10).foreach(_println)
    val map1 = date_dim.map(row => (row.head, row))
    val map2 = store_sales.map(row => (row.last, row))
    val join1 = map1.join(map2)
    join1.take(10).foreach(_println)
    val map3 = join1.map({
      case (_, (dd_row, ss_row)) =>
        (ss_row(1), (dd_row, ss_row))
    })
    val map4 = item.map(row => (row.head, row))
    val join2 = map3.join(map4)
    join2.take(10).foreach(_println)
    val map5 = join2.map({
      case (_, ((dd_row, ss_row), i_row)) =>
        (ss_row(2), (dd_row, ss_row, i_row))
    })
    val map6 = customer.map(row => (row.head, row))
    val join3 = map5.join(map6)
    join3.take(10).foreach(_println)
    val map7 = join3.map({
      case (_, ((dd_row, ss_row, i_row), c_row)) =>
        (c_row(4), (dd_row, ss_row, i_row, c_row))
    })
    val map8 = customer_address.map(row => (row.head, row))
    val join4 = map7.join(map8)
    join4.take(10).foreach(_println)
    val map9 = join4.map({
      case (_, ((dd_row, ss_row, i_row, c_row), ca_row)) =>
        (ss_row(6), (dd_row, ss_row, i_row, c_row, ca_row))
    })
    val map10 = store.map(row => (row.head, row))
    val join5 = map9.join(map10)
    join5.take(10).foreach(_println)
    val map11 = join5.map({
      case (_, ((dd_row, ss_row, i_row, c_row, ca_row), s_row)) =>
        (dd_row, ss_row, i_row, c_row, ca_row, s_row)
    })
    val filter1 = map11.filter({
      case (_, _, _, _, ca_row, s_row) =>
        val ca_zip = getColOrEmpty(ca_row, 9)
        val s_zip = s_row(25)
        ca_zip.take(5) > s_zip.take(5)
    })
    filter1.take(10).foreach(_println)
    val map12 = filter1.map({
      case (_, ss_row, i_row, _, _, _) =>
        val ss_ext_sales_price = convertColToFloat(ss_row, 14)
        val i_brand_id = i_row(7)
        val i_brand = i_row(8)
        val i_manufact_id = i_row(13)
        val i_manufact = i_row(14)
        ((i_brand_id, i_brand, i_manufact_id, i_manufact), ss_ext_sales_price)
    })
    val rbk1 = map12.reduceByKey(_ + _)
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