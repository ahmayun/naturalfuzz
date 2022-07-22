package utils

import abstraction.{SparkConf, SparkContext}
import runners.Config
import symbolicexecution.SymExResult

class FilterQueries(val symExResult: SymExResult) {

  val filterQueries: List[Query] = symExResult.getPathQueries // TODO: Might have redundant queries, filter out
  def getRows(datasets: Array[String]): List[QueryResult] = {
    val sc = new SparkContext(null)
    val rdds = datasets.map(sc.textFile)
    filterQueries.map(fq => fq.runQuery(rdds))
  }

  def createSatVectors(datasets: Array[String]): SatRDDs = {
    val sc = new SparkContext(null)
    val rdds = datasets.map(sc.textFile)
    val filterFns = filterQueries.map(_.tree.createFilterFn(0)) // TODO IMPORTANT: This assumes application uses only one dataset
    val satRDD = Array( rdds(0).map {
      row =>
        val cols = row.split(Config.delimiter)
        val sat = makeSatVector(filterFns, cols)
        cols.mkString(Config.delimiter) +
          Config.delimiter + sat.toBinaryString +
          Config.delimiter + sat.toString
    })
    println("satRDD")
    println(satRDD(0))
    new SatRDDs (
      satRDD
    , filterQueries)
  }

  def makeSatVector(conditions: List[Array[String] => Boolean], row: Array[String]): Int = {
    if (conditions.length > 32)
      throw new Exception("Too many conditions, unable to encode as 32 bit integer")

    conditions
      .map(f => f(row))
      .zipWithIndex
      .foldLeft(0) {
        case (acc, (b, i)) => acc | (toInt(b) << (31 - i))
      }
  }

  def toInt(bool: Boolean): Int = if(bool) 1 else 0
}
