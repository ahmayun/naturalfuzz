package examples.tpcds

import org.apache.spark.{SparkConf, SparkContext}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random

object Q13 extends Serializable {

  def main(args: Array[String]) {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 13").setMaster("spark://zion-headnode:7077")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
//    val datasetsPath = "./data_tpcds"
//    val seed = "ahmad".hashCode()
//    val rand = new Random(seed)
    val STATES = List("SC","AZ","LA","MN","NJ","DC","OR","VA","RI")
    val MS = List("M", "U", "D", "W", "S") // marital status
    val ES = List("Primary", "4 yr Degree", "Secondary", "College") // education status
    val BETWEEN = (v:Float, l:Float, u:Float) => v < u && v > l

    val p = "/TPCDS_1G_NOHEADER_NOCOMMAS"
    args(0) = s"$p/store_sales"
    val store_sales = sc.textFile(args(0)).map(_.split(","))
    args(1) = s"$p/store"
    val store = sc.textFile(args(1)).map(_.split(","))
    args(2) = s"$p/date_dim"
    val date_dim = sc.textFile(args(2)).map(_.split(","))
    args(3) = s"$p/household_demographics"
    val household_demographics = sc.textFile(args(3)).map(_.split(","))
    args(4) = s"$p/customer_demographics"
    val customer_demographics = sc.textFile(args(4)).map(_.split(","))
    args(5) = s"$p/customer_address"
    val customer_address = sc.textFile(args(5)).map(_.split(","))

    val map1 = store_sales.map(row => (row(6)/*ss_store_sk*/, row))
    val map3 = store.map(row => (row.head, row))
    val join1 = map1.join(map3)
    val map2 = join1.map {
        case (_, (ss_row, s_row)) =>
          (ss_row.last /*ss_sold_date*/, (ss_row, s_row))
      }
    val map4 = date_dim.map(row => (row.head, row))
    val join2 = map2.join(map4)
    val map5 = join2.map {
        case (_, ((ss_row, s_row), dd_row)) =>
          (ss_row(4)/*ss_hdemo_sk*/, (ss_row, s_row, dd_row))
      }
    val map9 = household_demographics.map(row => (row.head, row))
    val join3 = map5.join(map9)
    val map6 = join3.map {
        case (_, ((ss_row, s_row, dd_row), hd_row)) =>
          (ss_row(3)/*ss_cdemo_sk*/, (ss_row, s_row, dd_row, hd_row))
      }
    val map8 = customer_demographics.map(row => (row.head, row))
    val join4 = map6.join(map8)
    val map7 = join4.map {
        case (_, ((ss_row, s_row, dd_row, hd_row), cd_row)) =>
          (ss_row(5)/*ss_addr_sk*/, (ss_row, s_row, dd_row, hd_row, cd_row))
      }
    val map10 = customer_address.map(row => (row.head, row))
    val join5 = map7.join(map10)
      .map {
        case (_, ((ss_row, s_row, dd_row, hd_row, cd_row), ca_row)) =>
          (ss_row, s_row, dd_row, hd_row, cd_row, ca_row)
      }
    val filter1 = join5.filter {
        case (ss_row, s_row, dd_row, hd_row, cd_row, ca_row) =>
          val cd_marital_status = cd_row(2)
          val cd_education_status = cd_row(3)
          val ss_sales_price = convertColToFloat(ss_row, 12)
          val hd_dep_count = convertColToInt(hd_row, 3)
          val ca_country = ca_row(7)
          val ca_state = ca_row(8)
          val ss_net_profit = convertColToFloat(ss_row, ss_row.length-1)

          (
            (cd_marital_status == MS(0) && cd_education_status == ES(0) && BETWEEN(ss_sales_price, 100.0f, 150.0f) && hd_dep_count == 3) ||
              (cd_marital_status == MS(1) && cd_education_status == ES(1) && BETWEEN(ss_sales_price, 50.0f, 100.0f) && hd_dep_count == 1) ||
              (cd_marital_status == MS(2) && cd_education_status == ES(2) && BETWEEN(ss_sales_price, 150.0f, 200.0f) && hd_dep_count == 1)

            ) &&
            (
              (ca_country == "United States" && STATES.slice(0, 3).contains(ca_state) && BETWEEN(ss_net_profit, 100, 200)) ||
                (ca_country == "United States" && STATES.slice(3, 6).contains(ca_state) && BETWEEN(ss_net_profit, 150, 300)) ||
                (ca_country == "United States" && STATES.slice(6, 9).contains(ca_state) && BETWEEN(ss_net_profit, 50, 200))
            )

      }

    filter1.take(10).foreach(println)



    /*
define MS= ulist(dist(marital_status, 1, 1), 3);
     define ES= ulist(dist(education, 1, 1), 3);
     define STATE= ulist(dist(fips_county, 3, 1), 9);


     select avg(ss_quantity)
           ,avg(ss_ext_sales_price)
           ,avg(ss_ext_wholesale_cost)
           ,sum(ss_ext_wholesale_cost)
     from store_sales
         ,store
         ,customer_demographics
         ,household_demographics
         ,customer_address
         ,date_dim
     where s_store_sk = ss_store_sk
     and  ss_sold_date_sk = d_date_sk and d_year = 2001
     and((ss_hdemo_sk=hd_demo_sk
      and cd_demo_sk = ss_cdemo_sk
      and cd_marital_status = '[MS.1]'
      and cd_education_status = '[ES.1]'
      and ss_sales_price between 100.00 and 150.00
      and hd_dep_count = 3
         )or
         (ss_hdemo_sk=hd_demo_sk
      and cd_demo_sk = ss_cdemo_sk
      and cd_marital_status = '[MS.2]'
      and cd_education_status = '[ES.2]'
      and ss_sales_price between 50.00 and 100.00
      and hd_dep_count = 1
         ) or
         (ss_hdemo_sk=hd_demo_sk
      and cd_demo_sk = ss_cdemo_sk
      and cd_marital_status = '[MS.3]'
      and cd_education_status = '[ES.3]'
      and ss_sales_price between 150.00 and 200.00
      and hd_dep_count = 1
         ))
     and((ss_addr_sk = ca_address_sk
      and ca_country = 'United States'
      and ca_state in ('[STATE.1]', '[STATE.2]', '[STATE.3]')
      and ss_net_profit between 100 and 200
         ) or
         (ss_addr_sk = ca_address_sk
      and ca_country = 'United States'
      and ca_state in ('[STATE.4]', '[STATE.5]', '[STATE.6]')
      and ss_net_profit between 150 and 300
         ) or
         (ss_addr_sk = ca_address_sk
      and ca_country = 'United States'
      and ca_state in ('[STATE.7]', '[STATE.8]', '[STATE.9]')
      and ss_net_profit between 50 and 250
         ))
    ;
    */

  }

  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0.0f
    }
  }

  def convertColToInt(row: Array[String], col: Int): Int = {
    try {
      row(col).toInt
    } catch {
      case _ => 0
    }
  }

  def isBetween(date: String, start: String, end: String): Boolean = {
    try {
      val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      val givenDate = LocalDate.parse(date, formatter)
      val startDate = LocalDate.parse(start, formatter)
      val endDate = LocalDate.parse(end, formatter)
      givenDate.isAfter(startDate) && givenDate.isBefore(endDate)
    } catch {
      case _ => false
    }
  }

  /* ORIGINAL QUERY:
  define COUNTY = random(1, rowcount("active_counties", "store"), uniform);
  define STATE = distmember(fips_county, [COUNTY], 3);
  define YEAR = random(1998, 2002, uniform);
  define AGG_FIELD = text({"SR_RETURN_AMT",1},{"SR_FEE",1},{"SR_REFUNDED_CASH",1},{"SR_RETURN_AMT_INC_TAX",1},{"SR_REVERSED_CHARGE",1},{"SR_STORE_CREDIT",1},{"SR_RETURN_TAX",1});
  define _LIMIT=100;

  with customer_total_return as
  (
      select sr_customer_sk as ctr_customer_sk ,sr_store_sk as ctr_store_sk ,sum([AGG_FIELD])
                                                                                  as ctr_total_return
      from store_returns ,date_dim
      where sr_returned_date_sk = d_date_sk
      and d_year =[YEAR]
      group by sr_customer_sk ,sr_store_sk
  )
  [_LIMITA]

  select [_LIMITB] c_customer_id
  from customer_total_return ctr1 ,store ,customer
  where ctr1.ctr_total_return >   (
                                      -- subquery 1
                                      select avg(ctr_total_return)*1.2
                                      from customer_total_return ctr2
                                      where ctr1.ctr_store_sk = ctr2.ctr_store_sk
                                  )
  and s_store_sk = ctr1.ctr_store_sk
  and s_state = '[STATE]'
  and ctr1.ctr_customer_sk = c_customer_sk
  order by c_customer_id
  [_LIMITC];
   */
}