package examples.symbolic

import org.apache.spark.{SparkConf, SparkContext}
import sparkwrapper.SparkContextWithDP
import symbolicexecution.SymExResult

object RIGTestJoin extends Serializable {

  def main(args: Array[String]): SymExResult = {
    println(s"RIGTestJoin: ${args.mkString(",")}")
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[*]")
    sparkConf.setAppName("RIGTest Join")
    val ctx = new SparkContextWithDP(new SparkContext(sparkConf))
    ctx.setLogLevel("ERROR")
    val ds1p = "mixmatch-data/rig-test-join/boxes1"
    val ds2p = "mixmatch-data/rig-test-join/boxes2"

    val ds1 = ctx.textFileProv(ds1p, _.split(","))
//      .map(row => Array(row.head) ++ row.tail.map(_.toInt))
      .map(row => (row.head, row.tail.map(_.toInt)))
      .filter {
        row =>
          if(_root_.monitoring.Monitors.monitorPredicateSymEx(row._2(0) == 3, (List(row._2(0)), List()), 0)) {
            true
          } else {
            true
          }
      }
    val ds2 = ctx.textFileProv(ds2p, _.split(","))
//      .map(row => Array(row.head) ++ row.tail.map(_.toInt))
      .map(row => (row.head, row.tail.map(_.toInt)))
      .filter {
        row =>
          if (_root_.monitoring.Monitors.monitorPredicateSymEx(row._2(0) == 1, (List(row._2(0)), List()), 1)) {
            true
          } else {
            true
          }
      }
//    ds1.join(ds2)

    val joined = _root_.monitoring.Monitors.monitorJoinSymEx(ds1, ds2, 0) // PC: ds2.containsKey(ds1.col[0])

    joined.map {
      case row@(_, (a, b)) =>
        if (_root_.monitoring.Monitors.monitorPredicateSymEx(a(0) > b(0), (List(a(0), b(0)), List()), 2)) { // PC ds2.containsKey() && ds1.col[1] > ds2.col[1]
          if1()
        } else if (_root_.monitoring.Monitors.monitorPredicateSymEx(a(0) < b(0), (List(a(0), b(0)), List()), 3)) { // PC ds2.containsKey() && ds1.col[1] < ds2.col[1]
          if2()
        } else if (_root_.monitoring.Monitors.monitorPredicateSymEx(a(0) == b(0), (List(a(0), b(0)), List()), 4)) { // PC ds2.containsKey() && ds1.col[1] == ds2.col[1]
          if3()
        }
        row
    }
      .collect().foreach {
      case (key, (a, b)) =>
        println(key, a.mkString("-"), b.mkString("-"))
    }

    _root_.monitoring.Monitors.finalizeSymEx()
  }

  def if1(): Unit = {
    println("if 1")
  }

  def if2(): Unit = {
    println("if 2")
  }

  def if3(): Unit = {
    println("if 2")
  }

}