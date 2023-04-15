package examples.symbolic
import org.apache.spark.util.CollectionAccumulator
import org.apache.spark.{SparkConf, SparkContext}
import sparkwrapper.SparkContextWithDP
import taintedprimitives._
import taintedprimitives.SymImplicits._

import scala.util.Random
import sparkwrapper.SparkContextWithDP
import symbolicexecution.SymbolicExpression
import taintedprimitives._
import taintedprimitives.SymImplicits._
object Q6 extends Serializable {
  def main(args: Array[String], expressionAccumulator: CollectionAccumulator[SymbolicExpression]): Unit = {
    val conf = new SparkConf()
    conf.setMaster("local[*]")
    conf.setAppName("TPC-DS Query 1")
    val sc = new SparkContextWithDP(new SparkContext(conf))
    sc.setLogLevel("ERROR")
    val datasetsPath = "./data_tpcds"
    val MONTH = 1
    val YEAR = 2001
    val customer_address = sc.textFileProv(s"$datasetsPath/customer_address", _.split(","))
    val customer = sc.textFileProv(s"$datasetsPath/customer", _.split(","))
    val store_sales = sc.textFileProv(s"$datasetsPath/store_sales", _.split(","))
    val date_dim = sc.textFileProv(s"$datasetsPath/date_dim", _.split(","))
    val item = sc.textFileProv(s"$datasetsPath/item", _.split(","))
    val filter1 = date_dim.filter {
      row => _root_.monitoring.Monitors.monitorPredicateSymEx(row(6) == YEAR.toString && row(8) == MONTH.toString, (List(), List()), 0, expressionAccumulator)
    }
    val map1 = filter1.map(row => row(3))
    val distinct = map1.distinct
    val take1 = distinct.take(1).head
    val map2 = customer_address.map(row => (row.head, row))
    val map3 = customer.map(row => (row(4), row))
    val join1 = _root_.monitoring.Monitors.monitorJoin(map2, map3, 1)
    val map4 = join1.map({
      case (addr_sk, (ca_row, c_row)) =>
        (c_row.head, (ca_row, c_row))
    })
    val map5 = store_sales.map(row => (row(2), row))
    val join2 = _root_.monitoring.Monitors.monitorJoin(map4, map5, 2)
    val map6 = join2.map({
      case (customer_sk, ((ca_row, c_row), ss_row)) =>
        (ss_row.last, (ca_row, c_row, ss_row))
    })
    val map7 = date_dim.map(row => (row.head, row))
    val join3 = _root_.monitoring.Monitors.monitorJoin(map6, map7, 3)
    val map8 = join3.map({
      case (date_sk, ((ca_row, c_row, ss_row), dd_row)) =>
        (ss_row(1), (ca_row, c_row, ss_row, dd_row))
    })
    val map9 = item.map(row => (row.head, row))
    val join4 = _root_.monitoring.Monitors.monitorJoin(map8, map9, 4)
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
    val subquery2_result = reduce1._1 / reduce1._2
    val filter2 = map10.filter(tup => _root_.monitoring.Monitors.monitorPredicateSymEx(tup._4(3) == take1, (List(), List()), 5, expressionAccumulator))
    val filter3 = filter2.filter({
      case (_, _, _, _, i_row) =>
        val i_current_price = convertColToFloat(i_row, 5)
        _root_.monitoring.Monitors.monitorPredicateSymEx(i_current_price > 1.2f * subquery2_result, (List(), List()), 6, expressionAccumulator)
    })
    val map12 = filter3.map({
      case (ca_row, c_row, ss_row, dd_row, i_row) =>
        (try {
          ca_row(8)
        } catch {
          case _ => new TaintedString("NULL")
        }, 1)
    })
    val rbk1 = map12.reduceByKey(_ + _)
    val filter4 = rbk1.filter({
      case (state, count) =>
        count > 10
    })
    val sortBy1 = filter4.sortBy(_._2)
    val take2 = sortBy1.take(10)
    val sortWith1 = take2.sortWith({
      case (a, b) =>
        a._2 < b._2 || a._2 == b._2 && a._1 < b._1
    })
    sortWith1.foreach({
      case (state, count) =>
        println(state, count)
    })
    _root_.monitoring.Monitors.finalizeSymEx(expressionAccumulator)
  }
  def convertColToFloat(row: Array[TaintedString], col: TaintedInt): TaintedFloat = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0
    }
  }
}