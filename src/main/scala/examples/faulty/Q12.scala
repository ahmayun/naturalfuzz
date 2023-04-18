package examples.faulty

import abstraction.{SparkConf, SparkContext}

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object Q12 extends Serializable {

  def main(args: Array[String]) {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 12").setMaster("spark://zion-headnode:7077")
    val sc = SparkContext.getOrCreate(sparkConf)
    sc.setLogLevel("ERROR")
//    val datasetsPath = "./data_tpcds"
//    val seed = "ahmad".hashCode()
//    val rand = new Random(seed)
    val YEAR = 1999 // rand.nextInt(2003 - 1998) + 1998
    val START_DATE = s"$YEAR-01-01"
    val END_DATE = s"$YEAR-02-01"
    val CAT = List("Home", "Electronics", "Shoes") // many more

    val p = "/TPCDS_1G_NOHEADER_NOCOMMAS"
    args(0) = s"$p/web_sales"
    val web_sales = sc.textFile(args(0)).map(_.split(","))
    args(1) = s"$p/date_dim"
    val date_dim = sc.textFile(args(1)).map(_.split(","))
    args(2) = s"$p/item"
    val item = sc.textFile(args(2)).map(_.split(","))

    val filtered_item = item.filter {
      row =>
        val category = row(12)
        category == CAT(0) || category == CAT(1) || category == CAT(2)
    }
    filtered_item.take(10).foreach(println)

    val filtered_dd = date_dim.filter {
      row =>
        val d_date = row(2)
        isBetween(d_date, START_DATE, END_DATE)
    }
    filtered_dd.take(10).foreach(println)

    val map1 = web_sales.map(row => (row(2)/*ws_item_sk*/, row))
    val map7 = filtered_item.map(row => (row.head, row))
    val join1 = map1.join(map7)
    join1.take(10).foreach(println)

    val map2 = join1.map {
        case (item_sk, (ws_row, i_row)) =>
          (ws_row.last/*ws_sold_date*/, (ws_row, i_row))
      }
    val map8 = filtered_dd.map(row => (row.head, row))
    val join2 = map2.join(map8)
    join2.take(10).foreach(println)

    val map3 = join2.map {
        case (_, ((ws_row, i_row), dd_row)) =>
          val i_item_id = i_row(1)
          val i_item_desc = i_row(4)
          val i_category = i_row(12)
          val i_class = i_row(10)
          val i_current_price = i_row(5)
          val ws_ext_sales_price = convertColToFloat(ws_row, 22)


          ((i_item_id, i_item_desc, i_category, i_class, i_current_price), ws_ext_sales_price) // there should be another value here
      }

    val map4 = map3.map {
        case ((i_item_id, i_item_desc, i_category, i_class, i_current_price), ws_ext_sales_price) =>
          (i_class, ws_ext_sales_price)
      }
    val rbk1 = map4.reduceByKey(_+_)
    rbk1.take(10).foreach(println)

    val rbk2 = map3.reduceByKey(_ + _)
    rbk2.take(10).foreach(println)

    val map5 = rbk2.map {
        case ((i_item_id, i_item_desc, i_category, i_class, i_current_price), ws_ext_sales_price) =>
          (i_class, (i_item_id, i_item_desc, i_category, i_current_price, ws_ext_sales_price))
      }
    val join3 = map5.join(rbk1)
    join3.take(10).foreach(println)

    val map6 = join3.map {
        case (i_class, ((i_item_id, i_item_desc, i_category, i_current_price, ws_ext_sales_price), class_rev)) =>
          (i_item_id, i_item_desc, i_category, i_class, i_current_price, ws_ext_sales_price, ws_ext_sales_price/class_rev)
      }
    val sortBy1 = map6.sortBy(_._3)

    sortBy1.take(10).foreach(println)

    /*

    define YEAR=random(1998,2002,uniform);
    define SDATE=date([YEAR]+"-01-01",[YEAR]+"-07-01",sales);
    define CATEGORY=ulist(dist(categories,1,1),3);
    define _LIMIT=100;

    [_LIMITA] select [_LIMITB] i_item_id
          ,i_item_desc
          ,i_category
          ,i_class
          ,i_current_price
          ,sum(ws_ext_sales_price) as itemrevenue
          ,sum(ws_ext_sales_price)*100/sum(sum(ws_ext_sales_price)) over
              (partition by i_class) as revenueratio
    from
      web_sales
          ,item
          ,date_dim
    where
      ws_item_sk = i_item_sk
        and i_category in ('[CATEGORY.1]', '[CATEGORY.2]', '[CATEGORY.3]')
        and ws_sold_date_sk = d_date_sk
      and d_date between cast('[SDATE]' as date)
            and (cast('[SDATE]' as date) + 30 days)
    group by
      i_item_id
            ,i_item_desc
            ,i_category
            ,i_class
            ,i_current_price
    order by
      i_category
            ,i_class
            ,i_item_id
            ,i_item_desc
            ,revenueratio
    [_LIMITC];
    */

  }

  def convertColToFloat(row: Array[String], col: Int): Float = {
    try {
      row(col).toFloat
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