package examples.faulty

import abstraction.{SparkConf, SparkContext}
import capture.IOStreams._println

object Q6 extends Serializable {
  val MONTH = 1 // rand.nextInt(12)+1
  val YEAR = 2001 // rand.nextInt(2002 - 1998) + 1998

  def main(args: Array[String]) {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 6")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
//    val datasetsPath = "./data_tpcds"


    val customer_address = sc.textFile(args(0)).map(_.split(","))
    val customer = sc.textFile(args(1)).map(_.split(","))
    val store_sales = sc.textFile(args(2)).map(_.split(","))
    val date_dim = sc.textFile(args(3)).map(_.split(","))
    val item = sc.textFile(args(4)).map(_.split(","))


    val filter1 = date_dim.filter(filter1_f)
    filter1.take(10).foreach(_println)

    val map1 = filter1.map(map1_f)
    val distinct = map1.distinct
    val take1 = distinct.take(1).head
    val map2 = customer_address.map(map2_f)
    val map3 = customer.map(map3_f)
    val join1 = map2.join(map3)
    join1.take(10).foreach(_println)
    val map4 = join1.map(map4_f)
    val map5 = store_sales.map(map5_f)
    val join2 = map4.join(map5)
    join2.take(10).foreach(_println)
    val map6 = join2.map(map6_f)
    val map7 = date_dim.map(map7_f)
    val join3 = map6.join(map7)
    join3.take(10).foreach(_println)
    val map8 = join3.map(map8_f)
    val map9 = item.map(map9_f())
    val join4 = map8.join(map9)
    join4.take(10).foreach(_println)
    val map10 = join4.map(map10_f)
    val map11 = map10.map(map11_f)
    val reduce1 = map11.reduce(reduce1_f)
    _println(s"reduce1 = $reduce1")
    val subquery2_result = reduce1._1 / reduce1._2
    _println(s"subquery2 result = ${subquery2_result}")
    val filter2 = map10.filter(filter2_f)
    filter2.take(10).foreach(_println)
    val filter3 = filter2.filter(filter3_f)
    filter3.take(10).foreach(_println)
    val map12 = filter3.map(map12_f)
    val rbk1 = map12.reduceByKey(_ + _)
    rbk1.take(10).foreach(_println)
    val filter4 = rbk1.filter(filter4_f)
    filter4.take(10).foreach(_println)
    val sortBy1 = filter4.sortBy(_._2)
    val take2 = sortBy1.take(10)
    val sortWith1 = take2.sortWith { case (a, b) => (a._2 < b._2) || (a._2 == b._2 && a._1 < b._1) }
    sortWith1.foreach {
      case (state, count) => _println(state, count)
    }

  }

  def map1_f(row: Array[String]): String = {
    row(3) /*d_month_seq*/
  }

  def map2_f(row: Array[String]): (String, Array[String]) = {
    (row.head, row)
  }

  def map3_f(row: Array[String]): (String, Array[String]) = {
    (row(4) /*c_current_addr_sk*/ , row)
  }

  def map4_f(tup: (String, (Array[String], Array[String]))): (String, (Array[String], Array[String])) = {
    val (addr_sk, (ca_row, c_row)) = tup
    (c_row.head /*c_customer_sk*/ , (ca_row, c_row))
  }

  def map5_f(row: Array[String]): (String, Array[String]) = {
    (row(2) /*ss_customer_sk*/ , row)
  }

  def map6_f(tup: (String, ((Array[String], Array[String]), Array[String]))): (String, (Array[String], Array[String], Array[String])) = {
    val (customer_sk, ((ca_row, c_row), ss_row)) = tup
    (ss_row.last /*ss_sold_date_sk*/ , (ca_row, c_row, ss_row))
  }

  def map7_f(row: Array[String]): (String, Array[String]) = {
    (row.head /*d_date_sk*/ , row)
  }

  def map8_f(tup: (String, ((Array[String], Array[String], Array[String]), Array[String]))): (String, (Array[String], Array[String], Array[String], Array[String])) = {
    val (date_sk, ((ca_row, c_row, ss_row), dd_row)) = tup
    (ss_row(1) /*ss_item_sk*/ , (ca_row, c_row, ss_row, dd_row))
  }

  def map9_f(row: Array[String]): (String, Array[String]) = {
    (row.head /*i_item_sk*/ , row)
  }

  def map10_f(tup: (String, ((Array[String], Array[String], Array[String], Array[String]), Array[String]))): (Array[String], Array[String], Array[String], Array[String], Array[String]) = {
    val (item_sk, ((ca_row, c_row, ss_row, dd_row), i_row)) = tup
    (ca_row, c_row, ss_row, dd_row, i_row)
  }

  def map11_f(tup: (Array[String], Array[String], Array[String], Array[String], Array[String])): (Float, Int) = {
    val (_, _, _, _, i_row) = tup
    (convertColToFloat(i_row, 5), 1)
  }

  def filter2_f(tup: (Array[String], Array[String], Array[String], Array[String], Array[String])): Boolean = {
    tup._4(3) /*d_month_seq*/ == take1
  }

  def filter3_f(tup: (Array[String], Array[String], Array[String], Array[String], Array[String])): Boolean = {
    val (_, _, _, _, i_row) = tup
    val i_current_price = convertColToFloat(i_row, 5)
    i_current_price > 1.2 * subquery2_result
  }

  def map12_f(tup: (Array[String], Array[String], Array[String], Array[String], Array[String])): (String, Int) = {
    val (ca_row, c_row, ss_row, dd_row, i_row) = tup
    (try {
      ca_row(8) /*ca_state*/
    } catch {
      case _ => "NULL"
    }, 1) // Took some liberty here, ca_row(8) fails due to array out of bounds
  }

  def filter4_f(tup: (String, Int)): Boolean = {
    val (state, count) = tup
    count > 10
  }

  def filter1_f(row: Array[String]): Boolean = {
    row(6).toInt == YEAR && row(8).toInt == MONTH
  }

  def reduce1_f(acc: (Float, Int), e: (Float, Int)): (Float, Int) = {
    val (v1, c1) = acc
    val (v2, c2) = e
    (v1 + v2, c1 + c2)
  }

  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
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