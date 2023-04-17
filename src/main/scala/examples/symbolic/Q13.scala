package examples.symbolic
import org.apache.spark.util.CollectionAccumulator
import org.apache.spark.{SparkConf, SparkContext}
import sparkwrapper.SparkContextWithDP
import taintedprimitives._
import taintedprimitives.SymImplicits._

import java.time.LocalDate
import sparkwrapper.SparkContextWithDP
import taintedprimitives._
import taintedprimitives.SymImplicits._

import java.time.format.DateTimeFormatter
import sparkwrapper.SparkContextWithDP
import taintedprimitives._
import taintedprimitives.SymImplicits._

import scala.util.Random
import sparkwrapper.SparkContextWithDP
import taintedprimitives._
import taintedprimitives.SymImplicits._
import symbolicexecution.{SymExResult, SymbolicExpression}
object Q13 extends Serializable {
  def main(args: Array[String], expressionAccumulator: CollectionAccumulator[SymbolicExpression]): SymExResult = {
    val sparkConf = new SparkConf()
    sparkConf.setAppName("TPC-DS Query 13").setMaster("spark://zion-headnode:7077")
    val osc = SparkContext.getOrCreate(sparkConf)
    val sc = new SparkContextWithDP(osc)
    sc.setLogLevel("ERROR")
    val STATES = List("SC", "AZ", "LA", "MN", "NJ", "DC", "OR", "VA", "RI")
    val MS = List("M", "U", "D", "W", "S")
    val ES = List("Primary", "4 yr Degree", "Secondary", "College")
    val BETWEEN = (v: TaintedFloat, l: TaintedFloat, u: TaintedFloat) => v < u && v > l
//    val p = "/TPCDS_1G_NOHEADER_NOCOMMAS"
//    args(0) = s"$p/store_sales"
    val store_sales = sc.textFileProv(args(0), _.split(","))
//    args(1) = s"$p/store"
    val store = sc.textFileProv(args(1), _.split(","))
//    args(2) = s"$p/date_dim"
    val date_dim = sc.textFileProv(args(2), _.split(","))
//    args(3) = s"$p/household_demographics"
    val household_demographics = sc.textFileProv(args(3), _.split(","))
//    args(4) = s"$p/customer_demographics"
    val customer_demographics = sc.textFileProv(args(4), _.split(","))
//    args(5) = s"$p/customer_address"
    val customer_address = sc.textFileProv(args(5), _.split(","))
    val map1 = store_sales.map(row => (row(6), row))
    val map3 = store.map(row => (row.head, row))
    val join1 = _root_.monitoring.Monitors.monitorJoinSymEx(map1, map3, 0, expressionAccumulator)
    val map2 = join1.map({
      case (_, (ss_row, s_row)) =>
        (ss_row.last, (ss_row, s_row))
    })
    val map4 = date_dim.map(row => (row.head, row))
    val join2 = _root_.monitoring.Monitors.monitorJoinSymEx(map2, map4, 1, expressionAccumulator)
    val map5 = join2.map({
      case (_, ((ss_row, s_row), dd_row)) =>
        (ss_row(4), (ss_row, s_row, dd_row))
    })
    val map9 = household_demographics.map(row => (row.head, row))
    val join3 = _root_.monitoring.Monitors.monitorJoinSymEx(map5, map9, 2, expressionAccumulator)
    val map6 = join3.map({
      case (_, ((ss_row, s_row, dd_row), hd_row)) =>
        (ss_row(3), (ss_row, s_row, dd_row, hd_row))
    })
    val map8 = customer_demographics.map(row => (row.head, row))
    val join4 = _root_.monitoring.Monitors.monitorJoinSymEx(map6, map8, 3, expressionAccumulator)
    val map7 = join4.map({
      case (_, ((ss_row, s_row, dd_row, hd_row), cd_row)) =>
        (ss_row(5), (ss_row, s_row, dd_row, hd_row, cd_row))
    })
    val map10 = customer_address.map(row => (row.head, row))
    val join5 = _root_.monitoring.Monitors.monitorJoinSymEx(map7, map10, 4, expressionAccumulator).map({
      case (_, ((ss_row, s_row, dd_row, hd_row, cd_row), ca_row)) =>
        (ss_row, s_row, dd_row, hd_row, cd_row, ca_row)
    })
    join5.take(10).foreach(println)
    val filter1 = join5.filter({
      case (ss_row, s_row, dd_row, hd_row, cd_row, ca_row) =>
        val cd_marital_status = cd_row(2)
        val cd_education_status = cd_row(3)
        val ss_sales_price = convertColToFloat(ss_row, 12)
        val hd_dep_count = convertColToInt(hd_row, 3)
        _root_.monitoring.Monitors.monitorPredicateSymEx(
          (cd_marital_status == MS(0) && cd_education_status == ES(0) && BETWEEN(ss_sales_price, 100.0f, 150.0f) && hd_dep_count == 3
          || cd_marital_status == MS(1) && cd_education_status == ES(1) && BETWEEN(ss_sales_price, 50.0f, 100.0f) && hd_dep_count == 1
          || cd_marital_status == MS(2) && cd_education_status == ES(2) && BETWEEN(ss_sales_price, 150.0f, 200.0f) && hd_dep_count == 1),
          (List(), List()),
          5,
          expressionAccumulator
        )
    })
    val filter2 = filter1.filter({
      case (ss_row, s_row, dd_row, hd_row, cd_row, ca_row) =>
        val ca_country = getColOrEmpty(ca_row, 10)
        val ca_state = getColOrEmpty(ca_row, 8)
        val ss_net_profit = convertColToFloat(ss_row, ss_row.length - 1)
        _root_.monitoring.Monitors.monitorPredicateSymEx(
          (ca_country == "United States" && (ca_state == "SC" || ca_state == "AZ" || ca_state == "LA") && BETWEEN(ss_net_profit, 100, 200)
          || ca_country == "United States" && STATES.slice(3, 6).contains(ca_state) && BETWEEN(ss_net_profit, 150, 300)
          || ca_country == "United States" && STATES.slice(6, 9).contains(ca_state) && BETWEEN(ss_net_profit, 50, 200)),
          (List(), List()),
          6,
          expressionAccumulator
        )
    })
    filter2.take(10).foreach(println)
    _root_.monitoring.Monitors.finalizeSymEx(expressionAccumulator)
  }
  def convertColToFloat(row: Array[TaintedString], col: TaintedInt): TaintedFloat = {
    try {
      row(col).toFloat
    } catch {
      case _ => 0.0f
    }
  }
  def convertColToInt(row: Array[TaintedString], col: TaintedInt): TaintedInt = {
    try {
      row(col).toInt
    } catch {
      case _ => 0
    }
  }
  def isBetween(date: TaintedString, start: TaintedString, end: TaintedString): Boolean = {
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

  def getColOrEmpty(row: Array[TaintedString], col: TaintedInt): TaintedString = {
    try {
      row(col)
    } catch {
      case _: Throwable => ""
    }
  }
}