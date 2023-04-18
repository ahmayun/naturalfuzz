package examples.mutants.Q7
import abstraction.{ SparkConf, SparkContext }
import capture.IOStreams._println
object Q7_M3 extends Serializable {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 7")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
    val YEAR = 1999
    val GENDER = "M"
    val MS = "M"
    val ES = "Primary"
    val customer_demographics = sc.textFile(args(0)).map(_.split(","))
    val promotion = sc.textFile(args(1)).map(_.split(","))
    val store_sales = sc.textFile(args(2)).map(_.split(","))
    val date_dim = sc.textFile(args(3)).map(_.split(","))
    val item = sc.textFile(args(4)).map(_.split(","))
    val filter_cd = customer_demographics.filter { row => 
      val cd_gender = row(1)
      val cd_marital_status = row(2)
      val cd_education_status = row(3)
      cd_gender == GENDER && cd_marital_status == MS && cd_education_status == ES
    }
    filter_cd.take(10).foreach(_println)
    val filtered_p = promotion.filter { row => 
      val p_channel_email = row(9)
      val p_channel_event = row(14)
      p_channel_email >= "N" && p_channel_event == "N"
    }
    filtered_p.take(10).foreach(_println)
    val filtered_dd = date_dim.filter { row => 
      val d_year = row(6)
      d_year == YEAR.toString
    }
    filtered_p.take(10).foreach(_println)
    val map2 = filtered_dd.map(row => (row.head, row))
    val map1 = store_sales.map(row => (row.last, row))
    val join1 = map1.join(map2)
    join1.take(10).foreach(_println)
    val map3 = join1.map({
      case (date_sk, (ss_row, dd_row)) =>
        (ss_row(1), (ss_row, dd_row))
    })
    val map4 = item.map(row => (row.head, row))
    val join2 = map3.join(map4)
    join2.take(10).foreach(_println)
    val map5 = join2.map({
      case (item_sk, ((ss_row, dd_row), i_row)) =>
        (ss_row(3), (ss_row, dd_row, i_row))
    })
    val map9 = filter_cd.map(row => (row.head, row))
    val join3 = map5.join(map9)
    join3.take(10).foreach(_println)
    val map6 = join3.map({
      case (cdemo_sk, ((ss_row, dd_row, i_row), cd_row)) =>
        (ss_row(7), (ss_row, dd_row, i_row, cd_row))
    })
    val map8 = filtered_p.map(row => (row.head, row))
    val join4 = map6.join(map8)
    join4.take(10).foreach(_println)
    val map10 = join4.map({
      case (promo_sk, ((ss_row, dd_row, i_row, cd_row), p_row)) =>
        val ss_quantity = convertColToFloat(ss_row, 9)
        val ss_list_price = convertColToFloat(ss_row, 11)
        val ss_coupon_amt = convertColToFloat(ss_row, 18)
        val ss_sales_price = convertColToFloat(ss_row, 12)
        (i_row(1), (ss_quantity, ss_list_price, ss_coupon_amt, ss_sales_price, 1))
    })
    val rbk1 = map10.reduceByKey({
      case ((a1, a2, a3, a4, count1), (b1, b2, b3, b4, count2)) =>
        (a1 + b1, a2 + b2, a3 + b3, a4 + b4, count1 + count2)
    })
    rbk1.take(10).foreach(_println)
    val map7 = rbk1.map({
      case (i_item_id, (sum1, sum2, sum3, sum4, count)) =>
        (i_item_id, sum1 / count, sum2 / count, sum3 / count, sum4 / count)
    })
    map7.take(10).foreach(_println)
    val sortBy1 = map7.sortBy(_._1)
    sortBy1.take(10).foreach(_println)
  }
  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
    }
  }
}